package thesis.simulator.optimizer.operators;

import thesis.simulator.optimizer.Chromosome;

import java.util.*;

public class RepairOperator {
    private static final int MIN_GAP = 3; // minimum laps between consecutive stops

    public Chromosome repair(Chromosome c, int totalLaps) {
        // Step 1: clamp all lap genes to [1, totalLaps-1]
        for (int s = 0; s < 3; s++) {
            int li = 2 + s * 3;
            c.set(li, Math.max(1, Math.min(c.get(li), totalLaps - 1)));
        }

        // Step 2: sort active stops by lap ascending, pack into slots 1-2-3
        sortActiveStops(c);

        // Step 3: deduplicate lap values among active stops
        deduplicateLaps(c, totalLaps);

        // Step 4: eliminate stops whose new compound matches the compound in force
        eliminateRedundantCompounds(c);

        // Step 5: enforce minimum gap of MIN_GAP laps between consecutive active stops
        enforceMinGap(c);

        // Step 6: enforce 2-compound rule — at least 2 distinct slick compounds
        enforceTwoCompoundRule(c, totalLaps);

        // Step 7: final sort and compact (steps 4-6 may have deactivated some stops)
        sortActiveStops(c);

        return c;
    }
    private void sortActiveStops(Chromosome c) {
        List<int[]> active = new ArrayList<>(); // each entry: {lap, compound}
        for (int s = 0; s < 3; s++) {
            if (c.get(1 + s * 3) == 1) {
                active.add(new int[]{ c.get(2 + s * 3), c.get(3 + s * 3) });
            }
        }
        active.sort((a, b) -> Integer.compare(a[0], b[0]));

        // Write back sorted into slots 0, 1, 2; deactivate remaining
        for (int s = 0; s < 3; s++) {
            if (s < active.size()) {
                c.set(1 + s * 3, 1);
                c.set(2 + s * 3, active.get(s)[0]);
                c.set(3 + s * 3, active.get(s)[1]);
            } else {
                c.set(1 + s * 3, 0); // deactivate
            }
        }
    }
    private void deduplicateLaps(Chromosome c, int totalLaps) {
        for (int s = 1; s < 3; s++) {
            if (c.get(1 + s * 3) == 0) continue;         // not active
            if (c.get(1 + (s-1) * 3) == 0) continue;     // previous not active
            int prevLap = c.get(2 + (s-1) * 3);
            int curLap  = c.get(2 + s * 3);
            if (curLap <= prevLap) {
                int newLap = Math.min(prevLap + 1, totalLaps - 1);
                c.set(2 + s * 3, newLap);
            }
        }
    }
    private void eliminateRedundantCompounds(Chromosome c) {
        String prev = Chromosome.COMPOUNDS[Math.min(c.get(0), Chromosome.NUM_COMPOUNDS - 1)];
        for (int s = 0; s < 3; s++) {
            if (c.get(1 + s * 3) == 0) continue;
            String cmp = Chromosome.COMPOUNDS[Math.min(c.get(3 + s * 3), Chromosome.NUM_COMPOUNDS - 1)];
            if (cmp.equals(prev)) {
                c.set(1 + s * 3, 0); // deactivate
            } else {
                prev = cmp;
            }
        }
    }

    private void enforceMinGap(Chromosome c) {
        int prevLap = -MIN_GAP;
        for (int s = 0; s < 3; s++) {
            if (c.get(1 + s * 3) == 0) continue;
            int lap = c.get(2 + s * 3);
            if (lap - prevLap < MIN_GAP) {
                c.set(1 + s * 3, 0); // too close — deactivate
            } else {
                prevLap = lap;
            }
        }
    }

    private void enforceTwoCompoundRule(Chromosome c, int data_totalLaps) {
        // Collect all compounds in use
        Set<Integer> used = new LinkedHashSet<>();
        used.add(c.get(0) % Chromosome.NUM_COMPOUNDS);
        for (int s = 0; s < 3; s++) {
            if (c.get(1 + s * 3) == 1)
                used.add(c.get(3 + s * 3) % Chromosome.NUM_COMPOUNDS);
        }

        if (used.size() >= 2) return; // already legal

        // Find a compound NOT in `used`
        int newCmp = -1;
        for (int i = 0; i < Chromosome.NUM_COMPOUNDS; i++) {
            if (!used.contains(i)) { newCmp = i; break; }
        }
        if (newCmp == -1) return; // all three used — should not happen with NUM_COMPOUNDS=3

        // If there is at least one active stop, change its compound
        for (int s = 0; s < 3; s++) {
            if (c.get(1 + s * 3) == 1) {
                c.set(3 + s * 3, newCmp);
                return;
            }
        }
        // No active stops at all — force a 1-stop strategy at the midpoint
        int midLap = Math.max(1, c.get(2) % (data_totalLaps - 1)); // use lap gene even if inactive

        c.set(1, 1);       // activate stop 1
        c.set(2, midLap);  // use lap gene value
        c.set(3, newCmp);  // force the missing compound
    }


}
