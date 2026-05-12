package thesis.simulator.optimizer.operators;

import thesis.simulator.optimizer.Chromosome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Initializer {
    private final int totalLaps;
    private final Random rng;
    private final RepairOperator repair = new RepairOperator();

    public Initializer(int totalLaps, Random rng) {
        this.totalLaps = totalLaps;
        this.rng       = rng;
    }

    public List<Chromosome> initialize(int populationSize) {
        List<Chromosome> pop = new ArrayList<>(populationSize);
        int oneStop   = (int)(populationSize * 0.50);
        int twoStop   = (int)(populationSize * 0.40);
        int threeStop = populationSize - oneStop - twoStop;

        for (int i = 0; i < oneStop;   i++) pop.add(randomOneStop());
        for (int i = 0; i < twoStop;   i++) pop.add(randomTwoStop());
        for (int i = 0; i < threeStop; i++) pop.add(randomThreeStop());

        // Repair all — guarantees legality before first evaluation
        pop.forEach(c -> repair.repair(c, totalLaps));
        return pop;
    }
    private Chromosome randomOneStop() {
        int[] g = new int[Chromosome.LENGTH];
        int startCmp = rng.nextInt(Chromosome.NUM_COMPOUNDS);
        g[0] = startCmp;
        g[1] = 1; // stop 1 active
        g[2] = totalLaps / 4 + rng.nextInt(totalLaps / 2); // lap in [N/4, 3N/4]
        g[3] = differentCompound(startCmp);
        // stops 2 and 3 inactive
        return new Chromosome(g);
    }

    private Chromosome randomTwoStop() {
        int[] g = new int[Chromosome.LENGTH];
        int startCmp = rng.nextInt(Chromosome.NUM_COMPOUNDS);
        g[0] = startCmp;
        int lap1 = totalLaps / 5 + rng.nextInt(totalLaps / 5);        // ~[N/5, 2N/5]
        int lap2 = (totalLaps * 3 / 5) + rng.nextInt(totalLaps / 5);  // ~[3N/5, 4N/5]
        g[1] = 1; g[2] = lap1; g[3] = rng.nextInt(Chromosome.NUM_COMPOUNDS);
        g[4] = 1; g[5] = lap2; g[6] = rng.nextInt(Chromosome.NUM_COMPOUNDS);
        return new Chromosome(g);
    }

    private Chromosome randomThreeStop() {
        int[] g = new int[Chromosome.LENGTH];
        g[0] = rng.nextInt(Chromosome.NUM_COMPOUNDS);
        int third = totalLaps / 3;
        g[1] = 1; g[2] = third / 2 + rng.nextInt(third / 2); g[3] = rng.nextInt(Chromosome.NUM_COMPOUNDS);
        g[4] = 1; g[5] = third + rng.nextInt(third);          g[6] = rng.nextInt(Chromosome.NUM_COMPOUNDS);
        g[7] = 1; g[8] = 2 * third + rng.nextInt(third / 2);  g[9] = rng.nextInt(Chromosome.NUM_COMPOUNDS);
        return new Chromosome(g);
    }

    /** Returns any compound index different from `cmp`. */
    private int differentCompound(int cmp) {
        int other = rng.nextInt(Chromosome.NUM_COMPOUNDS - 1);
        if (other >= cmp) other++; // skip over cmp
        return other;
    }
}
