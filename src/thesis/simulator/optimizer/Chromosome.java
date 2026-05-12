package thesis.simulator.optimizer;

import thesis.simulator.models.PitStop;
import thesis.simulator.models.Strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Chromosome {
    public static final int LENGTH = 10;
    // The only compounds the GA works with (indices 0-2)
    public static final String[] COMPOUNDS = {"SOFT", "MEDIUM", "HARD"};
    public static final int NUM_COMPOUNDS  = COMPOUNDS.length;

    private final int[] genes;

    public Chromosome(int[] genes) {
        if (genes.length != LENGTH) throw new IllegalArgumentException("Chromosome must have " + LENGTH + " genes");
        this.genes = genes.clone();
    }

    public int get(int i)          { return genes[i]; }
    public void set(int i, int v)  { genes[i] = v; }
    public int[] getGenes()        { return genes.clone(); } // defensive copy
    public Chromosome copy()       { return new Chromosome(genes); }

    //Decode this chromosome into a Strategy for the given race length.
    public Strategy decode(int totalLaps) {
        String startCompound = COMPOUNDS[clampCompound(genes[0])];
        List<PitStop> stops  = new ArrayList<>();

        for (int s = 0; s < 3; s++) {
            int ai = 1 + s * 3;  // active flag index
            int li = 2 + s * 3;  // lap index
            int ci = 3 + s * 3;  // compound index
            if (genes[ai] == 1) {
                int lap = Math.max(1, Math.min(genes[li], totalLaps - 1));
                stops.add(new PitStop(lap, COMPOUNDS[clampCompound(genes[ci])]));
            }
        }
        stops.sort((a, b) -> Integer.compare(a.getLap(), b.getLap()));
        return new Strategy(startCompound, stops);
    }

    private int clampCompound(int c) {
        return Math.max(0, Math.min(c, NUM_COMPOUNDS - 1));
    }

    @Override
    public String toString() { return Arrays.toString(genes); }
}
