package thesis.simulator.models.monteCarlo;

import java.util.ArrayList;
import java.util.List;

public class MonteCarloResult {

    private final List<Double> raceTimes = new ArrayList<>();

    public void addRaceTime(double time) {
        raceTimes.add(time);
    }

    public List<Double> getRaceTimes() {
        return raceTimes;
    }

    public double getMean() {
        return raceTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    public double getMin() {
        return raceTimes.stream()
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(0.0);
    }

    public double getMax() {
        return raceTimes.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);
    }

    public double getStandardDeviation() {
        double mean = getMean();
        double variance = raceTimes.stream()
                .mapToDouble(t -> Math.pow(t - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }
}
