package thesis.simulator.models;

import java.util.ArrayList;
import java.util.List;

public class SimulationResult {
    private double totalRaceTime;
    private List<Double> lapTimes = new ArrayList<>();

    public void addLapTime(double time) {
        lapTimes.add(time);
        totalRaceTime += time;
    }

    public void addTime(double time) {
        totalRaceTime += time;
    }

    public double getTotalRaceTime() {
        return totalRaceTime;
    }

    public List<Double> getLapTimes() {
        return lapTimes;
    }
}
