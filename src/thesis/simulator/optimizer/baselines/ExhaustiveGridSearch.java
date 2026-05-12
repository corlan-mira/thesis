package thesis.simulator.optimizer.baselines;

import thesis.simulator.models.RaceData;
import thesis.simulator.models.Strategy;
import thesis.simulator.optimizer.*;
import thesis.simulator.optimizer.evaluation.FitnessEvaluator;
import thesis.simulator.optimizer.operators.RepairOperator;
import java.util.Random;

public class ExhaustiveGridSearch {
    private final FitnessEvaluator evaluator;
    private final RepairOperator   repair = new RepairOperator();
    private final int              totalLaps;
    private final int              lapStep; // quantization step (e.g., 3)

    public ExhaustiveGridSearch(RaceData data, String driver, int mcSamples, int lapStep, long seed) {
        this.evaluator = new FitnessEvaluator(data, driver, mcSamples);
        this.totalLaps = data.getTotalLaps();
        this.lapStep   = lapStep;
    }

    public Strategy run() {
        Chromosome best = null;
        double bestFitness = Double.MAX_VALUE;
        long scenarioSeed = 42L; // fixed for reproducibility of exhaustive baseline

        int nc = Chromosome.NUM_COMPOUNDS;
        // Enumerate all 1-stop and 2-stop strategies (3-stop optional, very slow)
        for (int c0 = 0; c0 < nc; c0++) {
            // 1-stop
            for (int l1 = lapStep; l1 < totalLaps; l1 += lapStep) {
                for (int c1 = 0; c1 < nc; c1++) {
                    int[] g = {c0, 1, l1, c1, 0, totalLaps/2, 0, 0, totalLaps/2, 0};
                    Chromosome ch = new Chromosome(g);
                    repair.repair(ch, totalLaps);
                    double f = evaluator.evaluate(ch, scenarioSeed);
                    if (f < bestFitness) { bestFitness = f; best = ch.copy(); }
                }
            }
            // 2-stop
            for (int l1 = lapStep; l1 < totalLaps - lapStep; l1 += lapStep) {
                for (int l2 = l1 + lapStep; l2 < totalLaps; l2 += lapStep) {
                    for (int c1 = 0; c1 < nc; c1++) {
                        for (int c2 = 0; c2 < nc; c2++) {
                            int[] g = {c0, 1, l1, c1, 1, l2, c2, 0, totalLaps/2, 0};
                            Chromosome ch = new Chromosome(g);
                            repair.repair(ch, totalLaps);
                            double f = evaluator.evaluate(ch, scenarioSeed);
                            if (f < bestFitness) { bestFitness = f; best = ch.copy(); }
                        }
                    }
                }
            }
        }
        return best == null ? null : best.decode(totalLaps);
    }
}
