package thesis.simulator.models;

import java.util.ArrayList;
import java.util.List;

public class Strategy {

    private String startCompound;
    private List<PitStop> pitStops = new ArrayList<>();

    public Strategy() {
    }

    public Strategy(String startCompound, List<PitStop> pitStops) {
        this.startCompound = startCompound;
        this.pitStops = pitStops;
    }

    public String getStartCompound() {
        return startCompound;
    }

    public void setStartCompound(String startCompound) {
        this.startCompound = startCompound;
    }

    public List<PitStop> getPitStops() {
        return pitStops;
    }

    public void setPitStops(List<PitStop> pitStops) {
        this.pitStops = pitStops;
    }

    public void addPitStop(PitStop pitStop) {
        this.pitStops.add(pitStop);
    }

    public PitStop getPitStopAtLap(int lap) {
        for (PitStop pitStop : pitStops) {
            if (pitStop.getLap() == lap) {
                return pitStop;
            }
        }
        return null;
    }
}