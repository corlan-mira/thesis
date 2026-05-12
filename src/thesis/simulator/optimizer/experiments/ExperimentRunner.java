package thesis.simulator.optimizer.experiments;

import thesis.simulator.dataClient.RaceDataClient;
import thesis.simulator.models.RaceData;
import thesis.simulator.models.Strategy;
import thesis.simulator.optimizer.GeneticAlgorithm;
import thesis.simulator.optimizer.OptimizerConfig;
import thesis.simulator.optimizer.baselines.RandomSearch;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;

public class ExperimentRunner {
    public static void main(String[] args) throws Exception {
        // --- Configure experiment ---
        String race   = "Monza";
        int    year   = 2023;
        String driver = "VER";
        int    runs   = 30;

        // --- Load race data ---
        RaceDataClient client = new RaceDataClient();
        RaceData data = client.getRaceData(year, race);
        System.out.println("Race data loaded: " + data);

        // --- Output file ---
        PrintWriter csv = new PrintWriter(new FileWriter(
                "experiment_" + race.replace(" ", "_") + "_" + year + ".csv"));
        csv.println("method,run,seed,best_race_time_s,wall_clock_ms");

        Random masterRng = new Random(12345L);
        OptimizerConfig cfg = new OptimizerConfig();

        // --- Run GA ---
        for (int run = 0; run < runs; run++) {
            long seed = masterRng.nextLong();
            long t0   = System.currentTimeMillis();
            GeneticAlgorithm ga = new GeneticAlgorithm(data, driver, cfg, seed);
            GeneticAlgorithm.Result result = ga.run();
            long elapsed = System.currentTimeMillis() - t0;
            csv.printf("GA,%d,%d,%.3f,%d%n", run, seed, result.bestExpectedTime, elapsed);
            System.out.printf("GA run %2d: %.2f s  (%d ms)%n", run, result.bestExpectedTime, elapsed);
        }

        // --- Run Random Search ---
        for (int run = 0; run < runs; run++) {
            long seed = masterRng.nextLong();
            long t0   = System.currentTimeMillis();
            RandomSearch rs = new RandomSearch(data, driver, cfg.mcSamplesPerEval, seed);
            Strategy s = rs.run(/* numEvals = */ 150_000 / cfg.mcSamplesPerEval);
            // re-evaluate with high M for fair comparison
            // ... (similar to GA final re-eval)
            long elapsed = System.currentTimeMillis() - t0;
            // log result
        }

        // --- Run Exhaustive (once — it's deterministic) ---
        // ...

        csv.flush();
        csv.close();
        System.out.println("Results written to CSV.");
    }
}
