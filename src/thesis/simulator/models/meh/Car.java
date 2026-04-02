package thesis.simulator.models.meh;

public class Car {
    private String teamName;
    private double performanceFactor;

    public Car(String teamName, double performanceFactor) {
        this.teamName = teamName;
        this.performanceFactor = performanceFactor;
    }

    public double getPerformanceFactor() {
        return performanceFactor;
    }

    public String getTeamName() {
        return teamName;
    }
}
