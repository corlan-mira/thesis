from flask import Flask, jsonify, request
import fastf1
import pandas as pd

app = Flask(__name__)

fastf1.Cache.enable_cache("cache")

def compute_performance(laps):
    clean_laps = laps.dropna(subset=["Driver", "LapTime"]).copy()

    avg_laps = clean_laps.groupby("Driver")["LapTime"].mean()

    if avg_laps.empty:
        return {}

    fastest = avg_laps.min()

    performance = {}

    for driver, lap in avg_laps.items():
        if pd.notna(lap) and pd.notna(fastest):
            performance[driver] = float(lap.total_seconds() / fastest.total_seconds())

    return performance


def compute_tire_degradation(laps):
    degradation = {}

    for compound in ["SOFT", "MEDIUM", "HARD"]:
        compound_laps = laps[
            (laps["Compound"] == compound) &
            (laps["LapTime"].notna())
        ].copy()

        if len(compound_laps) > 10:
            compound_laps["LapSeconds"] = compound_laps["LapTime"].dt.total_seconds()
            slope = compound_laps["LapSeconds"].diff().dropna().mean()

            if pd.notna(slope):
                degradation[compound] = float(slope)
            else:
                degradation[compound] = 0.0
        else:
            degradation[compound] = 0.0

    return degradation

def clean_dict(d):
    cleaned = {}
    for key, value in d.items():
        if pd.isna(value):
            cleaned[key] = 0.0
        else:
            cleaned[key] = float(value)
    return cleaned


@app.route("/race-data")
def race_data():
    year = int(request.args.get("year"))
    race = request.args.get("race")

    session = fastf1.get_session(year, race, "R")
    session.load()

    laps = session.laps
    weather = session.weather_data

    performance = compute_performance(laps)
    tire_deg = compute_tire_degradation(laps)

    base_lap = laps["LapTime"].dropna().mean()
    base_lap = float(base_lap.total_seconds()) if pd.notna(base_lap) else 0.0

    track_temp = weather["TrackTemp"].mean()
    rain_prob = weather["Rainfall"].mean()

    track_temp = float(track_temp) if pd.notna(track_temp) else 0.0
    rain_prob = float(rain_prob) if pd.notna(rain_prob) else 0.0

    return jsonify({
        "base_lap_time": base_lap,
        "performance_factor": performance,
        "tire_degradation": tire_deg,
        "track_temp": track_temp,
        "rain_probability": rain_prob
    })

if __name__ == "__main__":
    app.run(port=5000)