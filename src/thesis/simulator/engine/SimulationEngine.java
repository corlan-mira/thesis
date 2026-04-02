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

        String currentCompound = strategy.getStartCompound();

        for (int lap = 1; lap <= data.getTotalLaps(); lap++) {

            double lapTime = data.getBaseLapTime();
            lapTime += tireWear;
            lapTime *= performance;
            lapTime *= scenario.getWeatherMultiplier();
            //lap variation
            lapTime += random.nextGaussian() * scenario.getLapNoiseStd();
            //seafety car
            if (scenario.hasSafetyCar() && lap == scenario.getSafetyCarLap()) {
                lapTime *= 1.10;
            }
            //pit stop
            if (strategy.getPitStopAtLap(lap)!=null) {
                double pitLoss = scenario.getPitStopMean()
                        + random.nextGaussian() * scenario.getPitStopStd();
                totalTime += pitLoss;
                tireWear = 0.0;
                currentCompound = strategy.getPitStopAtLap(lap).getNewCompound();
            }
            //tyre deg
            Double deg = data.getTireDegradation().get(currentCompound.toUpperCase());
            if (deg == null) {
                deg = 0.1;
            }
            double effectiveDeg = deg + random.nextGaussian() * 0.01;
            effectiveDeg = Math.max(0.0, effectiveDeg);
            tireWear += effectiveDeg;
            totalTime += lapTime;
        }

        return totalTime;
    }
}
