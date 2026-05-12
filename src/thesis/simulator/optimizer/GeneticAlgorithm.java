package thesis.simulator.optimizer;

import thesis.simulator.models.RaceData;
import thesis.simulator.models.Strategy;
import thesis.simulator.optimizer.evaluation.FitnessEvaluator;
import thesis.simulator.optimizer.operators.*;

import java.util.*;

public class GeneticAlgorithm {
    private final OptimizerConfig cfg;
    private final FitnessEvaluator evaluator;
    private final RepairOperator repair;
    private final Initializer initializer;
    private final TournamentSelection selection;
    private final UniformCrossover crossover;
    private final GeneMutation mutation;
    private final Random rng;
    private final int              totalLaps;

    // Logging hook — set this before calling run() if you want per-generation callbacks
    public interface GenerationListener {
        void onGeneration(int gen, double bestFitness, double meanFitness, double worstFitness);
    }
    public GenerationListener listener; // optional

    public GeneticAlgorithm(RaceData data, String driver, OptimizerConfig cfg, long seed) {
        this.cfg        = cfg;
        this.totalLaps  = data.getTotalLaps();
        this.rng        = new Random(seed);
        this.evaluator  = new FitnessEvaluator(data, driver, cfg.mcSamplesPerEval);
        this.repair     = new RepairOperator();
        this.initializer= new Initializer(totalLaps, new Random(rng.nextLong()));
        this.selection  = new TournamentSelection(cfg.tournamentSize, new Random(rng.nextLong()));
        this.crossover  = new UniformCrossover(new Random(rng.nextLong()));
        this.mutation   = new GeneMutation(cfg.lapMutationStd, new Random(rng.nextLong()));
    }


    public static class Result {
        public final Strategy bestStrategy;
        public final double   bestExpectedTime; // from high-M re-evaluation
        public final int      generationsRun;
        public Result(Strategy s, double t, int g) {
            this.bestStrategy = s; this.bestExpectedTime = t; this.generationsRun = g;
        }
    }

    public Result run() {
        // --- Initialize ---
        List<Chromosome> population = initializer.initialize(cfg.populationSize);
        double[] fitness = new double[cfg.populationSize];

        long genSeed = rng.nextLong();
        for (int i = 0; i < cfg.populationSize; i++) {
            fitness[i] = evaluator.evaluate(population.get(i), genSeed);
        }

        double bestEver = Arrays.stream(fitness).min().getAsDouble();
        int    noImprovementCount = 0;
        int    finalGen = 0;

        // --- Generation loop ---
        for (int gen = 1; gen <= cfg.generations; gen++) {
            finalGen = gen;

            // New scenario seed for this generation (all evaluations use this — CRN)
            genSeed = rng.nextLong();

            // Find elites (top E by fitness)
            int[] eliteIdx = topKIndices(fitness, cfg.elitismCount);
            List<Chromosome> elites = new ArrayList<>();
            double[] eliteFitness   = new double[cfg.elitismCount];
            for (int e = 0; e < cfg.elitismCount; e++) {
                elites.add(population.get(eliteIdx[e]).copy());
                // Re-evaluate elites under new scenarioSeed — do not carry old fitness
                eliteFitness[e] = evaluator.evaluate(elites.get(e), genSeed);
            }

            // Build offspring until we have (P - E) new individuals
            List<Chromosome> offspring = new ArrayList<>();
            while (offspring.size() < cfg.populationSize - cfg.elitismCount) {
                Chromosome p1 = selection.select(population, fitness);
                Chromosome p2 = selection.select(population, fitness);

                Chromosome[] children;
                if (rng.nextDouble() < cfg.crossoverRate) {
                    children = crossover.crossover(p1, p2);
                } else {
                    children = new Chromosome[]{ p1.copy(), p2.copy() };
                }

                for (Chromosome child : children) {
                    if (rng.nextDouble() < cfg.mutationRate) {
                        mutation.mutate(child, totalLaps);
                    }
                    repair.repair(child, totalLaps); // always repair
                    offspring.add(child);
                    if (offspring.size() >= cfg.populationSize - cfg.elitismCount) break;
                }
            }

            // Evaluate offspring under the SAME genSeed as elites (CRN!)
            double[] offspringFitness = new double[offspring.size()];
            for (int i = 0; i < offspring.size(); i++) {
                offspringFitness[i] = evaluator.evaluate(offspring.get(i), genSeed);
            }

            // Assemble new population: elites + offspring
            population.clear();
            fitness = new double[cfg.populationSize];
            for (int e = 0; e < cfg.elitismCount; e++) {
                population.add(elites.get(e));
                fitness[e] = eliteFitness[e];
            }
            for (int i = 0; i < offspring.size(); i++) {
                population.add(offspring.get(i));
                fitness[cfg.elitismCount + i] = offspringFitness[i];
            }

            // Logging
            double genBest  = Arrays.stream(fitness).min().getAsDouble();
            double genMean  = Arrays.stream(fitness).average().getAsDouble();
            double genWorst = Arrays.stream(fitness).max().getAsDouble();
            if (listener != null) listener.onGeneration(gen, genBest, genMean, genWorst);

            // Early stopping
            if (genBest < bestEver - cfg.convergenceEps) {
                bestEver = genBest;
                noImprovementCount = 0;
            } else {
                noImprovementCount++;
            }
            if (noImprovementCount >= cfg.convergenceWindow) break;
        }

        // --- Final re-evaluation: top 5 with high-M ---
        int[] top5 = topKIndices(fitness, Math.min(5, cfg.populationSize));
        FitnessEvaluator finalEval = new FitnessEvaluator(
                /* data */ evaluator.getData(), // expose via getter or pass through
                /* driver */ evaluator.getDriver(),
                cfg.mcSamplesFinal
        );
        long finalSeed = rng.nextLong();
        int bestIdx = top5[0];
        double bestFinalFitness = Double.MAX_VALUE;
        for (int idx : top5) {
            double f = finalEval.evaluate(population.get(idx), finalSeed);
            if (f < bestFinalFitness) {
                bestFinalFitness = f;
                bestIdx = idx;
            }
        }

        Strategy best = population.get(bestIdx).decode(totalLaps);
        return new Result(best, bestFinalFitness, finalGen);
    }

    private int[] topKIndices(double[] fitness, int k) {
        Integer[] indices = new Integer[fitness.length];
        for (int i = 0; i < indices.length; i++) indices[i] = i;
        Arrays.sort(indices, Comparator.comparingDouble(i -> fitness[i]));
        int[] result = new int[k];
        for (int i = 0; i < k; i++) result[i] = indices[i];
        return result;
    }
}
