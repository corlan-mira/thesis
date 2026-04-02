import os
import threading
import time
from typing import Any

from flask import Flask, jsonify, request
import pandas as pd
import fastf1
from fastf1.livetiming.data import LiveTimingData

# Configuration
LIVE_FILE = os.environ.get("FASTF1_LIVE_FILE", "race_live.txt")
CACHE_DIR = os.environ.get("FASTF1_CACHE_DIR", "cache_live")
REFRESH_SECONDS = int(os.environ.get("FASTF1_REFRESH_SECONDS", "30"))

fastf1.Cache.enable_cache(CACHE_DIR)

app = Flask(__name__)


class LiveTimingStore:

    def __init__(self, live_file: str):
        self.live_file = live_file
        self.lock = threading.Lock()
        self.last_loaded_at = None
        self.last_error = None
        self.categories = []
        self.snapshot = {
            "session_loaded": False,
            "positions": [],
            "latest_laps": [],
            "stints": [],
            "summary": {},
        }

    def _safe_timedelta_seconds(self, value: Any):
        if pd.isna(value):
            return None
        try:
            return float(value.total_seconds())
        except Exception:
            return None

    def _safe_value(self, value: Any):
        if pd.isna(value):
            return None
        if isinstance(value, pd.Timestamp):
            return value.isoformat()
        if isinstance(value, pd.Timedelta):
            return self._safe_timedelta_seconds(value)
        try:
            # numpy scalars, etc.
            if hasattr(value, "item"):
                return value.item()
        except Exception:
            pass
        return value

    def _safe_records(self, df: pd.DataFrame):
        records = []
        for _, row in df.iterrows():
            record = {}
            for col, val in row.items():
                record[col] = self._safe_value(val)
            records.append(record)
        return records

    def _extract_positions(self, session) -> list[dict]:
        """
        Uses session.results when available.
        For a live/partial session this may be incomplete, but it is the cleanest normalized source.
        """
        try:
            results = session.results.copy()
            if results is None or results.empty:
                return []

            cols = [
                c for c in [
                    "Abbreviation",
                    "FullName",
                    "TeamName",
                    "Position",
                    "ClassifiedPosition",
                    "GridPosition",
                    "Status",
                    "Points",
                    "Laps"
                ] if c in results.columns
            ]

            out = results[cols].copy()

            # Prefer live race position if available; otherwise sorting may be partial
            if "Position" in out.columns:
                out = out.sort_values(by="Position", na_position="last")

            return self._safe_records(out.reset_index(drop=True))
        except Exception:
            return []

    def _extract_latest_laps(self, session) -> list[dict]:
        """
        Latest completed lap per driver from session.laps.
        """
        try:
            laps = session.laps.copy()
            if laps is None or laps.empty:
                return []

            keep_cols = [
                c for c in [
                    "Driver",
                    "DriverNumber",
                    "LapNumber",
                    "LapTime",
                    "Compound",
                    "Stint",
                    "TyreLife",
                    "Position",
                    "TrackStatus",
                    "PitInTime",
                    "PitOutTime",
                    "IsPersonalBest"
                ] if c in laps.columns
            ]

            if "Driver" not in laps.columns or "LapNumber" not in laps.columns:
                return []

            latest = (
                laps.sort_values(by=["Driver", "LapNumber"])
                .groupby("Driver", as_index=False)
                .tail(1)
            )

            latest = latest[keep_cols].copy()

            if "Position" in latest.columns:
                latest = latest.sort_values(by="Position", na_position="last")
            else:
                latest = latest.sort_values(by="Driver")

            return self._safe_records(latest.reset_index(drop=True))
        except Exception:
            return []

    def _extract_current_stints(self, session) -> list[dict]:
        """
        Derives the current stint per driver from the latest lap entry.
        """
        try:
            laps = session.laps.copy()
            if laps is None or laps.empty:
                return []

            needed = [c for c in [
                "Driver",
                "LapNumber",
                "Compound",
                "Stint",
                "TyreLife",
                "Position"
            ] if c in laps.columns]

            if "Driver" not in laps.columns:
                return []

            latest = (
                laps.sort_values(by=["Driver", "LapNumber"])
                .groupby("Driver", as_index=False)
                .tail(1)
            )

            latest = latest[needed].copy()

            rename_map = {
                "LapNumber": "CurrentLap",
                "Compound": "CurrentCompound",
                "TyreLife": "CurrentTyreLife"
            }
            latest = latest.rename(columns=rename_map)

            if "Position" in latest.columns:
                latest = latest.sort_values(by="Position", na_position="last")
            else:
                latest = latest.sort_values(by="Driver")

            return self._safe_records(latest.reset_index(drop=True))
        except Exception:
            return []

    def _extract_summary(self, session) -> dict:
        """
        Session-level summary for the frontend / Java app.
        """
        summary = {}

        try:
            summary["event_name"] = getattr(session.event, "EventName", None)
        except Exception:
            summary["event_name"] = None

        try:
            summary["year"] = getattr(session.event, "Year", None)
        except Exception:
            summary["year"] = None

        try:
            summary["session_name"] = session.name
        except Exception:
            summary["session_name"] = None

        try:
            laps = session.laps
            if laps is not None and not laps.empty and "LapNumber" in laps.columns:
                summary["max_observed_lap"] = int(pd.to_numeric(
                    laps["LapNumber"], errors="coerce"
                ).max())
            else:
                summary["max_observed_lap"] = None
        except Exception:
            summary["max_observed_lap"] = None

        try:
            summary["driver_count"] = len(session.drivers) if session.drivers is not None else 0
        except Exception:
            summary["driver_count"] = 0

        return summary

    def reload(self):
        with self.lock:
            try:
                if not os.path.exists(self.live_file):
                    raise FileNotFoundError(f"Live timing file not found: {self.live_file}")

                livedata = LiveTimingData(self.live_file)
                self.categories = livedata.list_categories()

                # FastF1's documented pattern for using saved live timing data
                # is to load it into a session through livedata=...
                session = fastf1.get_testing_session(2021, 1, 1)
                session.load(livedata=livedata)

                positions = self._extract_positions(session)
                latest_laps = self._extract_latest_laps(session)
                stints = self._extract_current_stints(session)
                summary = self._extract_summary(session)

                self.snapshot = {
                    "session_loaded": True,
                    "positions": positions,
                    "latest_laps": latest_laps,
                    "stints": stints,
                    "summary": summary,
                }

                self.last_loaded_at = time.time()
                self.last_error = None

            except Exception as e:
                self.last_error = str(e)

    def get_state(self):
        with self.lock:
            return {
                "live_file": self.live_file,
                "last_loaded_at": self.last_loaded_at,
                "last_error": self.last_error,
                "categories": list(self.categories),
                **self.snapshot
            }


store = LiveTimingStore(LIVE_FILE)


def background_reloader():
    while True:
        store.reload()
        time.sleep(REFRESH_SECONDS)


@app.route("/health")
def health():
    state = store.get_state()
    return jsonify({
        "ok": state["last_error"] is None,
        "live_file": state["live_file"],
        "last_loaded_at": state["last_loaded_at"],
        "last_error": state["last_error"],
        "session_loaded": state["session_loaded"]
    })


@app.route("/categories")
def categories():
    state = store.get_state()
    return jsonify({
        "categories": state["categories"],
        "last_error": state["last_error"]
    })


@app.route("/session-summary")
def session_summary():
    state = store.get_state()
    return jsonify({
        "summary": state["summary"],
        "last_error": state["last_error"]
    })


@app.route("/current-positions")
def current_positions():
    state = store.get_state()
    return jsonify({
        "positions": state["positions"],
        "count": len(state["positions"]),
        "last_error": state["last_error"]
    })


@app.route("/latest-laps")
def latest_laps():
    limit = request.args.get("limit", default=None, type=int)
    state = store.get_state()
    laps = state["latest_laps"]
    if limit is not None:
        laps = laps[:limit]

    return jsonify({
        "latest_laps": laps,
        "count": len(laps),
        "last_error": state["last_error"]
    })


@app.route("/current-stints")
def current_stints():
    state = store.get_state()
    return jsonify({
        "stints": state["stints"],
        "count": len(state["stints"]),
        "last_error": state["last_error"]
    })


if __name__ == "__main__":
    # Initial load
    store.reload()

    # Background refresh thread
    thread = threading.Thread(target=background_reloader, daemon=True)
    thread.start()

    app.run(host="0.0.0.0", port=5001, debug=False)