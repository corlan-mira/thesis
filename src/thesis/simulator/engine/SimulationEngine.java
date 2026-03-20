package thesis.simulator.engine;

import thesis.simulator.models.*;

import java.util.Random;

public class SimulationEngine {

    public double simulateRace(RaceData data, Strategy strategy, String driver,
                               RaceScenario scenario, Random random) {

        double totalTime = 0.0;
        double tireWear = 0.0;

        Double performance = data.getPerformanceFactor().get(driver);
        if (performance == null) {
            performance = 1.0;
        }

        String currentCompound = strategy.getCurrentCompound();

        for (int lap = 1; lap <= 50; lap++) {

            double lapTime = data.getBaseLapTime();

            lapTime += tireWear;
            lapTime *= performance;
            lapTime *= scenario.getWeatherMultiplier();

            // Random lap variation
            lapTime += random.nextGaussian() * scenario.getLapNoiseStd();

            // Safety car effect
            if (scenario.hasSafetyCar() && lap == scenario.getSafetyCarLap()) {
                lapTime *= 1.10;
            }

            // Pit stop
            if (strategy.isPitLap(lap)) {
                double pitLoss = scenario.getPitStopMean()
                        + random.nextGaussian() * scenario.getPitStopStd();

                totalTime += pitLoss;
                tireWear = 0.0;
                currentCompound = strategy.getNextCompound();
            }

            Double deg = data.getTireDegradation().get(currentCompound.toUpperCase());
            if (deg == null) {
                deg = 0.1;
            }

            // Add degradation noise
            double effectiveDeg = deg + random.nextGaussian() * 0.01;
            effectiveDeg = Math.max(0.0, effectiveDeg);

            tireWear += effectiveDeg;
            totalTime += lapTime;
        }

        return totalTime;
    }
}
