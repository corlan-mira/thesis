package thesis.simulator.optimizer.baselines;
import thesis.simulator.models.RaceData;
import thesis.simulator.models.Strategy;
import thesis.simulator.optimizer.*;
import thesis.simulator.optimizer.evaluation.FitnessEvaluator;
import thesis.simulator.optimizer.operators.RepairOperator;
import java.util.Random;

public class RandomSearch {
    private final FitnessEvaluator evaluator;
    private final RepairOperator   repair = new RepairOperator();
    private final int              totalLaps;
    private final Random           rng;

    public RandomSearch(RaceData data, String driver, int mcSamples, long seed) {
        this.evaluator = new FitnessEvaluator(data, driver, mcSamples);
        this.totalLaps = data.getTotalLaps();
        this.rng       = new Random(seed);
    }

    /** Run random search with a budget of `numEvals` evaluations. */
    public Strategy run(int numEvals) {
        Chromosome best = null;
        double bestFitness = Double.MAX_VALUE;
        long scenarioSeed = rng.nextLong(); // one fixed seed for fair comparison with GA

        for (int i = 0; i < numEvals; i++) {
            Chromosome c = randomChromosome();
            repair.repair(c, totalLaps);
            double f = evaluator.evaluate(c, scenarioSeed);
            if (f < bestFitness) { bestFitness = f; best = c.copy(); }
        }

        return best == null ? null : best.decode(totalLaps);
    }

    private Chromosome randomChromosome() {
        int[] g = new int[Chromosome.LENGTH];
        g[0] = rng.nextInt(Chromosome.NUM_COMPOUNDS); // start compound
        for (int s = 0; s < 3; s++) {
            g[1 + s * 3] = rng.nextInt(2);                          // active flag
            g[2 + s * 3] = 1 + rng.nextInt(totalLaps - 1);          // lap
            g[3 + s * 3] = rng.nextInt(Chromosome.NUM_COMPOUNDS);   // compound
        }
        return new Chromosome(g);
    }
}
