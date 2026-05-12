package thesis.simulator.optimizer.operators;

import thesis.simulator.optimizer.Chromosome;

import java.util.Random;

public class GeneMutation {
    private final double lapStd;  // σ for Gaussian lap perturbation
    private final Random rng;

    public GeneMutation(double lapStd, Random rng) {
        this.lapStd = lapStd;
        this.rng    = rng;
    }

    /**
     * Mutate exactly one randomly chosen gene of the chromosome.
     * The chromosome is modified in-place and returned.
     */
    public Chromosome mutate(Chromosome c, int totalLaps) {
        int gene = rng.nextInt(Chromosome.LENGTH);

        if (gene == 0) {
            // Start compound — replace with a uniformly sampled other compound
            c.set(0, differentCompound(c.get(0)));

        } else {
            int slot = (gene - 1) / 3;  // which pit stop slot (0, 1, or 2)
            int type = (gene - 1) % 3;  // 0=active, 1=lap, 2=compound

            if (type == 0) {
                // Active flag — flip
                c.set(gene, 1 - c.get(gene));

            } else if (type == 1) {
                // Lap — Gaussian perturbation
                int lap = c.get(gene);
                int newLap = (int) Math.round(lap + rng.nextGaussian() * lapStd);
                newLap = Math.max(1, Math.min(newLap, totalLaps - 1));
                c.set(gene, newLap);

            } else {
                // Compound — replace with a uniformly sampled other compound
                c.set(gene, differentCompound(c.get(gene)));
            }
        }
        return c;
    }

    private int differentCompound(int cmp) {
        int other = rng.nextInt(Chromosome.NUM_COMPOUNDS - 1);
        if (other >= cmp) other++;
        return other;
    }
}
