package thesis.simulator.models;

public class RaceScenario {

    private final double lapNoiseStd;
    private final double pitStopMean;
    private final double pitStopStd;
    private final boolean safetyCar;
    private final int safetyCarLap;
    private final double weatherMultiplier;

    public RaceScenario(double lapNoiseStd,
                        double pitStopMean,
                        double pitStopStd,
                        boolean safetyCar,
                        int safetyCarLap,
                        double weatherMultiplier) {
        this.lapNoiseStd = lapNoiseStd;
        this.pitStopMean = pitStopMean;
        this.pitStopStd = pitStopStd;
        this.safetyCar = safetyCar;
        this.safetyCarLap = safetyCarLap;
        this.weatherMultiplier = weatherMultiplier;
    }

    public double getLapNoiseStd() {
        return lapNoiseStd;
    }

    public double getPitStopMean() {
        return pitStopMean;
    }

    public double getPitStopStd() {
        return pitStopStd;
    }

    public boolean hasSafetyCar() {
        return safetyCar;
    }

    public int getSafetyCarLap() {
        return safetyCarLap;
    }

    public double getWeatherMultiplier() {
        return weatherMultiplier;
    }
}