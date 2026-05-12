package thesis.simulator.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class RaceData {

    @JsonProperty("base_lap_time")
    private double baseLapTime;

    @JsonProperty("performance_factor")
    private Map<String, Double> performanceFactor;

    @JsonProperty("tire_degradation")
    private Map<String, Double> tireDegradation;

    @JsonProperty("tire_degradation_quadratic")
    private Map<String, Double> tireDegradationQuadratic;

    @JsonProperty("track_temp")
    private double trackTemp;

    @JsonProperty("rain_probability")
    private double rainProbability;

    @JsonProperty("compound_offsets")
    private Map<String, Double> compoundOffsets;

    @JsonProperty("total_laps")
    private int totalLaps;

    public RaceData() {
    }

    public double getBaseLapTime() {
        return baseLapTime;
    }

    public void setBaseLapTime(double baseLapTime) {
        this.baseLapTime = baseLapTime;
    }

    public Map<String, Double> getPerformanceFactor() {
        return performanceFactor;
    }

    public void setPerformanceFactor(Map<String, Double> performanceFactor) {
        this.performanceFactor = performanceFactor;
    }

    public Map<String, Double> getTireDegradation() {
        return tireDegradation;
    }

    public void setTireDegradation(Map<String, Double> tireDegradation) {
        this.tireDegradation = tireDegradation;
    }

    public Map<String, Double> getTireDegradationQuadratic() {
        return tireDegradationQuadratic;
    }

    public void setTireDegradationQuadratic(Map<String, Double> tireDegradationQuadratic) {
        this.tireDegradationQuadratic = tireDegradationQuadratic;
    }

    public double getTrackTemp() {
        return trackTemp;
    }

    public void setTrackTemp(double trackTemp) {
        this.trackTemp = trackTemp;
    }

    public double getRainProbability() {
        return rainProbability;
    }

    public void setRainProbability(double rainProbability) {
        this.rainProbability = rainProbability;
    }

    public Map<String, Double> getCompoundOffsets() {
        return compoundOffsets;
    }

    public void setCompoundOffsets(Map<String, Double> compoundOffsets) {
        this.compoundOffsets = compoundOffsets;
    }

    public int getTotalLaps() {return totalLaps;}
    public void setTotalLaps(int totalLaps) {this.totalLaps = totalLaps;}

    @Override
    public String toString() {
        return "RaceData{" +
                "baseLapTime=" + baseLapTime +
                ", performanceFactor=" + performanceFactor +
                ", tireDegradation=" + tireDegradation +
                ", tireDegradationQuadratic=" + tireDegradationQuadratic +
                ", compoundOffsets=" + compoundOffsets +
                ", trackTemp=" + trackTemp +
                ", rainProbability=" + rainProbability +
                ", totalLaps=" + totalLaps +
                '}';
    }
}