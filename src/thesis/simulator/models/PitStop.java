package thesis.simulator.models;
public class PitStop {

    private int lap;
    private String newCompound;

    public PitStop() {
    }

    public PitStop(int lap, String newCompound) {
        this.lap = lap;
        this.newCompound = newCompound;
    }

    public int getLap() {
        return lap;
    }

    public void setLap(int lap) {
        this.lap = lap;
    }

    public String getNewCompound() {
        return newCompound;
    }

    public void setNewCompound(String newCompound) {
        this.newCompound = newCompound;
    }
}
