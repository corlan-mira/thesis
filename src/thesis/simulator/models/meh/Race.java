package thesis.simulator.models.meh;

public class Race {
    private Track track;
    private Car car;
    private int totalLaps;


    public Race(Track track, Car car, int totalLaps) {
        this.track = track;
        this.car = car;
        this.totalLaps = totalLaps;
    }

    public Track getTrack() {
        return track;
    }

    public Car getCar() {
        return car;
    }

    public int getTotalLaps() {
        return totalLaps;
    }
}
