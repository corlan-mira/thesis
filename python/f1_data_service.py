from flask import Flask, jsonify, request
import fastf1
import pandas as pd

app = Flask(__name__)

fastf1.Cache.enable_cache("cache")


def safe_mean(series, default=0.0):
    value = series.dropna().mean()
    if pd.isna(value):
        return default
    return float(value)


def weighted_average(values, weights):
    if not values or not weights or len(values) != len(weights):
        return 0.0
    total_weight = sum(weights)
    if total_weight == 0:
        return 0.0
    return float(sum(v * w for v, w in zip(values, weights)) / total_weight)


def clean_numeric_dict(data):
    cleaned = {}
    for key, value in data.items():
        if pd.isna(value):
            cleaned[key] = 0.0
        else:
            cleaned[key] = float(value)
    return cleaned


def compute_driver_pace_from_laps(laps):
    clean_laps = laps.dropna(subset=["Driver", "LapTime"]).copy()
    if clean_laps.empty:
        return {}
    avg_laps = clean_laps.groupby("Driver")["LapTime"].mean()
    if avg_laps.empty:
        return {}
    fastest = avg_laps.min()
    if pd.isna(fastest):
        return {}
    performance = {}
    for driver, lap in avg_laps.items():
        if pd.notna(lap):
            performance[driver] = float(lap.total_seconds() / fastest.total_seconds())
    return performance


def compute_tire_degradation_from_laps(laps):
    degradation = {}
    for compound in ["SOFT", "MEDIUM", "HARD"]:
        compound_laps = laps[
            (laps["Compound"] == compound) &
            (laps["LapTime"].notna())
            ].copy()
        if len(compound_laps) <= 10:
            degradation[compound] = 0.0
            continue
        compound_laps["LapSeconds"] = compound_laps["LapTime"].dt.total_seconds()
        compound_laps["LapNumberDiff"] = compound_laps["LapNumber"].diff()
        compound_laps["LapTimeDiff"] = compound_laps["LapSeconds"].diff()
        valid_steps = compound_laps[
            (compound_laps["LapNumberDiff"] == 1) &
            (compound_laps["LapTimeDiff"].notna())
            ]
        if valid_steps.empty:
            degradation[compound] = 0.0
            continue
        slope = valid_steps["LapTimeDiff"].mean()
        if pd.isna(slope):
            degradation[compound] = 0.0
        else:
            degradation[compound] = float(slope)
    return degradation


def get_same_race_history_sessions(target_year, race, max_races=5):
    sessions = []
    for year in range(target_year - 1, 2017, -1):
        try:
            session = fastf1.get_session(year, race, "R")
            session.load()
            sessions.append(session)
        except Exception:
            continue
        if len(sessions) >= max_races:
            break
    return sessions


def get_recent_season_sessions(target_year, race, max_races=5):
    sessions = []
    try:
        target_event = fastf1.get_event(target_year, race)
        target_round = int(target_event["RoundNumber"])
    except Exception:
        return sessions
    for round_number in range(target_round - 1, 0, -1):
        try:
            session = fastf1.get_session(target_year, round_number, "R")
            session.load()
            sessions.append(session)
        except Exception:
            continue
        if len(sessions) >= max_races:
            break
    return sessions

def merge_driver_factors(recent_form_factors, track_history_factors,
                         recent_weight=0.85, track_weight=0.15):
    all_drivers = set(recent_form_factors.keys()) | set(track_history_factors.keys())
    if not all_drivers:
        return {}
    merged = {}
    recent_default = max(recent_form_factors.values(), default=1.05)
    track_default = max(track_history_factors.values(), default=1.05)
    for driver in all_drivers:
        recent_value = recent_form_factors.get(driver, recent_default)
        track_value = track_history_factors.get(driver, track_default)
        merged[driver] = float(
            recent_weight * recent_value +
            track_weight * track_value
        )
    return merged

def aggregate_driver_performance_from_sessions(sessions):
    if not sessions:
        return {}
    driver_history = {}
    total_sessions = len(sessions)
    for index, session in enumerate(sessions):
        laps = session.laps
        session_factors = compute_driver_pace_from_laps(laps)
        weight = total_sessions - index
        for driver, factor in session_factors.items():
            driver_history.setdefault(driver, []).append((factor, weight))
    aggregated = {}
    for driver, values in driver_history.items():
        factors = [factor for factor, _ in values]
        weights = [weight for _, weight in values]
        aggregated[driver] = weighted_average(factors, weights)
    return clean_numeric_dict(aggregated)


def aggregate_tire_degradation_from_sessions(sessions):
    if not sessions:
        return {"SOFT": 0.0, "MEDIUM": 0.0, "HARD": 0.0}
    compound_history = {
        "SOFT": [],
        "MEDIUM": [],
        "HARD": []
    }
    total_sessions = len(sessions)
    for index, session in enumerate(sessions):
        laps = session.laps
        session_deg = compute_tire_degradation_from_laps(laps)
        weight = total_sessions - index
        for compound in ["SOFT", "MEDIUM", "HARD"]:
            compound_history[compound].append((session_deg.get(compound, 0.0), weight))
    aggregated = {}
    for compound, values in compound_history.items():
        factors = [factor for factor, _ in values]
        weights = [weight for _, weight in values]
        aggregated[compound] = weighted_average(factors, weights)
    return clean_numeric_dict(aggregated)


def estimate_base_lap_time_from_sessions(sessions):
    if not sessions:
        return 0.0
    values = []
    weights = []
    total_sessions = len(sessions)
    for index, session in enumerate(sessions):
        laps = session.laps
        avg_lap = laps["LapTime"].dropna().mean()
        if pd.notna(avg_lap):
            values.append(float(avg_lap.total_seconds()))
            weights.append(total_sessions - index)
    return weighted_average(values, weights)


def estimate_weather_from_sessions(sessions):
    if not sessions:
        return 0.0, 0.0
    track_temps = []
    rain_probs = []
    weights = []
    total_sessions = len(sessions)
    for index, session in enumerate(sessions):
        weather = session.weather_data
        if weather.empty:
            continue
        track_temp = safe_mean(weather["TrackTemp"], default=0.0)
        rain_prob = safe_mean(weather["Rainfall"], default=0.0)
        track_temps.append(track_temp)
        rain_probs.append(rain_prob)
        weights.append(total_sessions - index)
    estimated_track_temp = weighted_average(track_temps, weights)
    estimated_rain_prob = weighted_average(rain_probs, weights)
    return estimated_track_temp, estimated_rain_prob


@app.route("/race-data")
def race_data():
    year = int(request.args.get("year"))
    race = request.args.get("race")
    same_track_sessions = get_same_race_history_sessions(year, race, max_races=5)
    recent_season_sessions = get_recent_season_sessions(year, race, max_races=5)
    tire_deg = aggregate_tire_degradation_from_sessions(same_track_sessions)
    track_driver_factors = aggregate_driver_performance_from_sessions(same_track_sessions)
    recent_driver_factors = aggregate_driver_performance_from_sessions(recent_season_sessions)
    performance = merge_driver_factors(
        recent_driver_factors,
        track_driver_factors,
        recent_weight=0.85,
        track_weight=0.15
    )

    base_lap = estimate_base_lap_time_from_sessions(same_track_sessions)
    track_temp, rain_prob = estimate_weather_from_sessions(same_track_sessions)

    return jsonify({
        "base_lap_time": float(base_lap),
        "performance_factor": clean_numeric_dict(performance),
        "tire_degradation": clean_numeric_dict(tire_deg),
        "track_temp": float(track_temp),
        "rain_probability": float(rain_prob),
        "total_laps": int(laps["LapNumber"].max())
    })


if __name__ == "__main__":
    app.run(port=5000)