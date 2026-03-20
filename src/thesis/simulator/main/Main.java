package thesis.simulator.main;

import thesis.simulator.models.*;
import thesis.simulator.dataClient.*;
import thesis.simulator.models.monteCarlo.MonteCarloResult;
import thesis.simulator.models.monteCarlo.MonteCarloRunner;


public class Main {

    public static void main(String[] args) {
        try {
            RaceData raceData = RaceDataClient.getRaceData(2023, "Monza");

            Strategy strategy = new Strategy();
            strategy.setPitLap(25);
            strategy.setStartCompound("MEDIUM");
            strategy.setNextCompound("HARD");

            MonteCarloRunner runner = new MonteCarloRunner();
            MonteCarloResult result = runner.run(raceData, strategy, "VER", 500);

            System.out.println("Monte Carlo finished.");
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
