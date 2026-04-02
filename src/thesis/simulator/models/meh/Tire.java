package thesis.simulator.models.meh;

import thesis.simulator.models.TireCompound;

public class Tire {

        private TireCompound compound;
        private double wear;

        public Tire(TireCompound compound) {
            this.compound = compound;
            this.wear = 0.0;
        }

        public void increaseWear() {
            wear += compound.getDegradationRate();
        }

        public double getWearPenalty() {
            return wear * 2.0;
        }

        public TireCompound getCompound() {
            return compound;
        }

        public void reset(TireCompound newCompound) {
            this.compound = newCompound;
            this.wear = 0.0;
        }
}

