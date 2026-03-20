package thesis.simulator.models;

public class PitStop {
    private int lap;
    private TireCompound newCompound;
    //[ S | P1 | C1 | P2 | C2 ]

    public PitStop(int lap, TireCompound newCompound) {
        this.lap = lap;
        this.newCompound = newCompound;
    }

    public int getLap() {
        return lap;
    }

    public TireCompound getNewCompound() {
        return newCompound;
    }
}
