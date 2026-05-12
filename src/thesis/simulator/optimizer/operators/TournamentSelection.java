package thesis.simulator.optimizer.operators;

import thesis.simulator.optimizer.Chromosome;

import java.util.List;
import java.util.Random;

public class TournamentSelection {
    private final int k;
    private final Random rng;

    public TournamentSelection(int k, Random rng) {
        this.k   = k;
        this.rng = rng;
    }

    public Chromosome select(List<Chromosome> population, double[] fitness) {
        int best = rng.nextInt(population.size());
        for (int i = 1; i < k; i++) {
            int candidate = rng.nextInt(population.size());
            if (fitness[candidate] < fitness[best]) best = candidate;
        }
        return population.get(best).copy();
    }
}
