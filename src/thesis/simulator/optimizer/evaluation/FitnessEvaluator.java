package thesis.simulator.optimizer.evaluation;

import thesis.simulator.models.RaceData;
import thesis.simulator.models.Strategy;
import thesis.simulator.models.monteCarlo.MonteCarloResult;
import thesis.simulator.models.monteCarlo.MonteCarloRunner;
import thesis.simulator.optimizer.Chromosome;

public class FitnessEvaluator {
    private final MonteCarloRunner runner = new MonteCarloRunner();
    private final RaceData data;
    private final String driver;
    private final int m;

    public FitnessEvaluator(RaceData data, String driver, int m) {
        this.data   = data;
        this.driver = driver;
        this.m      = m;
    }


    public double evaluate(Chromosome c, long scenarioSeed) {
        Strategy s = c.decode(data.getTotalLaps());
        // noiseSeed varies per call — only scenarioSeed needs to be shared for CRN
        long noiseSeed = System.nanoTime();
        MonteCarloResult result = runner.run(data, s, driver, m, scenarioSeed, noiseSeed);
        return result.getMean();
    }

    public RaceData getData() {return data;}
    public String getDriver() {return driver;}
}
