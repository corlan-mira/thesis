package thesis.simulator.engine.validator;

import thesis.simulator.models.PitStop;
import thesis.simulator.models.RaceData;
import thesis.simulator.models.Strategy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimulationValidator {

    public static class Result {
        public final boolean valid;
        public final String reason;  // null if valid
        public Result(boolean v, String r) { this.valid = v; this.reason = r; }
    }

    public static Result validateForDryRace(Strategy s, RaceData data) {
        if (s.getStartCompound() == null)
            return new Result(false, "No start compound");

        List<PitStop> stops = s.getPitStops();

        // Pit laps in valid range
        for (PitStop p : stops) {
            if (p.getLap() < 1 || p.getLap() >= data.getTotalLaps())
                return new Result(false, "Pit lap out of range: " + p.getLap());
        }

        // No duplicate pit laps
        Set<Integer> laps = new HashSet<>();
        for (PitStop p : stops) {
            if (!laps.add(p.getLap()))
                return new Result(false, "Duplicate pit lap: " + p.getLap());
        }

        // 2-compound rule (dry only — skip this in wet-declared races)
        Set<String> compounds = new HashSet<>();
        compounds.add(s.getStartCompound().toUpperCase());
        for (PitStop p : stops) compounds.add(p.getNewCompound().toUpperCase());
        Set<String> slicks = new HashSet<>();
        for (String c : compounds)
            if (c.equals("SOFT") || c.equals("MEDIUM") || c.equals("HARD")) slicks.add(c);
        if (slicks.size() < 2)
            return new Result(false, "Must use at least 2 different slick compounds");

        return new Result(true, null);
    }
}
