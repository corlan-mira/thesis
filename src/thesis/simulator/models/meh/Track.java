package thesis.simulator.models.meh;

public class Track {
    private String name;
    private double baseLapTime;
    private double pitLaneLoss;
    //noOfMinimumPitStops
    //recommendedPitStops

    public Track(String name, double baseLapTime, double pitLaneLoss) {
        this.name = name;
        this.baseLapTime = baseLapTime;
        this.pitLaneLoss = pitLaneLoss;
    }

    public double getBaseLapTime() {
        return baseLapTime;
    }

    public double getPitLaneLoss() {
        return pitLaneLoss;
    }

    public String getName() {
        return name;
    }
}
