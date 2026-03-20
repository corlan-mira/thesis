package thesis.simulator.models.monteCarlo;

import thesis.simulator.engine.SimulationEngine;
import thesis.simulator.models.RaceData;
import thesis.simulator.models.RaceScenario;
import thesis.simulator.models.Strategy;

import java.util.Random;

public class MonteCarloRunner {

    private final SimulationEngine engine = new SimulationEngine();

    public MonteCarloResult run(RaceData data, Strategy strategy, String driver, int iterations) {
        MonteCarloResult result = new MonteCarloResult();
        Random random = new Random();

        for (int i = 0; i < iterations; i++) {
            RaceScenario scenario = generateScenario(random);

            double raceTime = engine.simulateRace(data, strategy, driver, scenario, random);
            result.addRaceTime(raceTime);
        }

        return result;
    }

    private RaceScenario generateScenario(Random random) {
        double lapNoiseStd = 0.15;
        double pitStopMean = 20.0;
        double pitStopStd = 0.4;

        boolean safetyCar = random.nextDouble() < 0.20;
        int safetyCarLap = 10 + random.nextInt(35);

        double weatherMultiplier;
        double roll = random.nextDouble();

        if (roll < 0.70) {
            weatherMultiplier = 1.00; // dry
        } else if (roll < 0.90) {
            weatherMultiplier = 1.02; // cloudy/cooler
        } else {
            weatherMultiplier = 1.06; // light rain
        }

        return new RaceScenario(
                lapNoiseStd,
                pitStopMean,
                pitStopStd,
                safetyCar,
                safetyCarLap,
                weatherMultiplier
        );
    }
}
