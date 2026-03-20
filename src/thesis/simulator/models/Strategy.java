package thesis.simulator.models;

import java.util.List;

public class Strategy {

    public int pitLap;
    public String startCompound;
    public String nextCompound;

    public boolean isPitLap(int lap) {
        return lap == pitLap;
    }

    public String getCurrentCompound() {
        return nextCompound;
    }

    public String getNextCompound() {
        return nextCompound;
    }

    public void setPitLap(int pitLap) {
        this.pitLap = pitLap;
    }

    public void setStartCompound(String startCompound) {
        this.startCompound = startCompound;
    }

    public void setNextCompound(String nextCompound) {
        this.nextCompound = nextCompound;
    }
}