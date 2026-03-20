package thesis.simulator.models;

public enum TireCompound {
    SOFT(0.08, 0.0),
    MEDIUM(0.05, 0.3),
    HARD(0.03, 0.6);

    private final double degradationRate;
    private final double baseTimePenalty;

    TireCompound(double degradationRate, double baseTimePenalty) {
        this.degradationRate = degradationRate;
        this.baseTimePenalty = baseTimePenalty;
    }

    public double getDegradationRate() {
        return degradationRate;
    }

    public double getBaseTimePenalty() {
        return baseTimePenalty;
    }
}
