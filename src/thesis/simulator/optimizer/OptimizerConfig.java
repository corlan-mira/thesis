package thesis.simulator.optimizer;

public class OptimizerConfig {
    public int    populationSize    = 50;
    public int    generations       = 100;
    public double crossoverRate     = 0.85;
    public double mutationRate      = 0.15;   // per-chromosome
    public int    tournamentSize    = 4;
    public int    elitismCount      = 2;
    public int    mcSamplesPerEval  = 30;
    public int    mcSamplesFinal    = 500;
    public double lapMutationStd    = 3.0;    // Gaussian std for lap gene mutation
    public int    maxPitStops       = 3;      // K_max
    public double convergenceEps    = 0.5;    // seconds — early-stop threshold
    public int    convergenceWindow = 20; //number of generations with no improvements in fitness allowed
}
