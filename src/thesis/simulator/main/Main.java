package thesis.simulator.main;

import thesis.simulator.models.*;
import thesis.simulator.dataClient.*;
import thesis.simulator.models.monteCarlo.MonteCarloResult;
import thesis.simulator.models.monteCarlo.MonteCarloRunner;

import java.util.Map;


public class Main {

    public static void main(String[] args) {
        try {
            RaceData raceData = RaceDataClient.getRaceData(2023, "Monza");

            Map<String, Double> performanceFactors=raceData.getPerformanceFactor();
            System.out.println("Performance factors, monza 2023: ");
            System.out.println(performanceFactors.toString());

            Strategy strategy = new Strategy();
            strategy.setStartCompound("MEDIUM");
            strategy.CreatePitStop(20, "MEDIUM");

            MonteCarloRunner runner = new MonteCarloRunner();
            MonteCarloResult result = runner.run(raceData, strategy, "VER", 1000);

            System.out.println("Monte Carlo finished without seed, Monza 2023, m-h.");
            System.out.println("Runs: " + result.getRaceTimes().size());
            System.out.println("Mean race time: " + result.getMean());
            System.out.println("Min race time: " + result.getMin());
            System.out.println("Max race time: " + result.getMax());
            System.out.println("Std deviation: " + result.getStandardDeviation());

            long seed = 42L;
            result = runner.run(raceData, strategy, "VER", 1000, seed);
            System.out.println("Monte Carlo finished try one with seed 42, Monza 2023, m-h. with seed: " + seed);
            System.out.println("Runs: " + result.getRaceTimes().size());
            System.out.println("Mean race time: " + result.getMean());
            System.out.println("Min race time: " + result.getMin());
            System.out.println("Max race time: " + result.getMax());
            System.out.println("Std deviation: " + result.getStandardDeviation());

            result = runner.run(raceData, strategy, "VER", 1000, seed);
            System.out.println("Monte Carlo finished try two with seed 42, Monza 2023, m-h. with seed: " + seed);
            System.out.println("Runs: " + result.getRaceTimes().size());
            System.out.println("Mean race time: " + result.getMean());
            System.out.println("Min race time: " + result.getMin());
            System.out.println("Max race time: " + result.getMax());
            System.out.println("Std deviation: " + result.getStandardDeviation());

            seed=32L;

            result = runner.run(raceData, strategy, "VER", 1000, seed);
            System.out.println("Monte Carlo finished try two with seed 32, Monza 2023, m-h. with seed: " + seed);
            System.out.println("Runs: " + result.getRaceTimes().size());
            System.out.println("Mean race time: " + result.getMean());
            System.out.println("Min race time: " + result.getMin());
            System.out.println("Max race time: " + result.getMax());
            System.out.println("Std deviation: " + result.getStandardDeviation());

            Strategy strategy2=new Strategy();
            strategy2.setStartCompound("HARD");
            strategy.CreatePitStop(15, "SOFT");

            result = runner.run(raceData, strategy2, "VER", 500);

            System.out.println("Monte Carlo finished, Monza 2023, s-h.");
            System.out.println("Runs: " + result.getRaceTimes().size());
            System.out.println("Mean race time: " + result.getMean());
            System.out.println("Min race time: " + result.getMin());
            System.out.println("Max race time: " + result.getMax());
            System.out.println("Std deviation: " + result.getStandardDeviation());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
