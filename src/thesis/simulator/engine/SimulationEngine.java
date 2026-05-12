package thesis.simulator.engine;

import thesis.simulator.models.*;

import java.util.Random;

public class SimulationEngine {
    public static double FUEL_EFFECT_PER_LAP=0.055;

    public double simulateRace(RaceData data, Strategy strategy, String driver,
                               RaceScenario scenario, Random random) {

        double totalTime = 0.0;
        int tireAge=0;

        Double performance = data.getPerformanceFactor().get(driver);
        if (performance == null) {
            performance = 1.0;
        }

        String currentCompound = strategy.getStartCompound();

        for (int lap = 1; lap <= data.getTotalLaps(); lap++) {

            double lapTime = data.getBaseLapTime();
            double compoundOffset = 0.0;
            if (data.getCompoundOffsets() != null) {
                Double value = data.getCompoundOffsets().get(currentCompound.toUpperCase());
                if (value != null) {
                    compoundOffset = value;
                }
            }
            lapTime += compoundOffset;
            double a = 0.05;  // fallback linear coefficient if data missing
            double b = 0.0;   // fallback quadratic coefficient if data missing
            if (data.getTireDegradation() != null) {
                Double aVal = data.getTireDegradation().get(currentCompound.toUpperCase());
                if (aVal != null) a = aVal;
            }
            if (data.getTireDegradationQuadratic() != null) {
                Double bVal = data.getTireDegradationQuadratic().get(currentCompound.toUpperCase());
                if (bVal != null) b = bVal;
            }
            double wearPenalty = a * tireAge + b * tireAge * tireAge;
            //noise grows with tire age — older tires have more variable pace
            double wearNoise = random.nextGaussian() * 0.01 * Math.sqrt(Math.max(1, tireAge));
            wearPenalty = Math.max(0.0, wearPenalty + wearNoise);
            lapTime += wearPenalty;
            lapTime -= FUEL_EFFECT_PER_LAP+(lap-1);
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
                tireAge = 0;
                currentCompound = strategy.getPitStopAtLap(lap).getNewCompound();
            }else{
                tireAge++;
            }
            totalTime += lapTime;
        }

        return totalTime;
    }
}
