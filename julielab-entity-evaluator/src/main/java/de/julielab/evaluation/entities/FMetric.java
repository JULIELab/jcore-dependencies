package de.julielab.evaluation.entities;

public class FMetric {
    private double recall;
    private double precision;
    private double f;

    public FMetric(double recall, double precision, double f) {
        this.recall = recall;
        this.precision = precision;
        this.f = f;
    }

    public FMetric(int tps, int fps, int fns) {
        this.recall = FMeasure.getRecall(tps, fps, fns);
        this.precision = FMeasure.getPrecision(tps, fps, fns);
        this.f = FMeasure.getFMeasure(tps, fps, fns);
    }

    public double getRecall() {
        return recall;
    }

    public double getPrecision() {
        return precision;
    }

    public double getF() {
        return f;
    }
}
