package thesis.simulator.optimizer.operators;

import thesis.simulator.optimizer.Chromosome;
import java.util.Random;


public class UniformCrossover {
    private final Random rng;

    public UniformCrossover(Random rng) { this.rng = rng; }

    /**
     * Produce two children from two parents via uniform crossover.
     * Each gene is independently inherited from parent1 (50%) or parent2 (50%).
     * Returns the two children as a two-element array.
     */
    public Chromosome[] crossover(Chromosome p1, Chromosome p2) {
        int[] g1 = p1.getGenes();
        int[] g2 = p2.getGenes();
        int[] c1 = new int[Chromosome.LENGTH];
        int[] c2 = new int[Chromosome.LENGTH];

        for (int i = 0; i < Chromosome.LENGTH; i++) {
            if (rng.nextBoolean()) {
                c1[i] = g1[i]; c2[i] = g2[i]; // p1→child1, p2→child2
            } else {
                c1[i] = g2[i]; c2[i] = g1[i]; // swap
            }
        }
        return new Chromosome[]{ new Chromosome(c1), new Chromosome(c2) };
    }
}
