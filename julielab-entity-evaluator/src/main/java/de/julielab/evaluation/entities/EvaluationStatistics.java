package de.julielab.evaluation.entities;

public class EvaluationStatistics {
    public int fp;
    public int fn;
    public int tp;

    public EvaluationStatistics(int tp, int fp, int fn) {
        this.tp = tp;
        this.fp = fp;
        this.fn = fn;
    }

    public int getFp() {
        return fp;
    }

    public int getFn() {
        return fn;
    }

    public int getTp() {
        return tp;
    }

    public EvaluationStatistics incrementTp(int delta) {
        this.tp += delta;
        return this;
    }

    public EvaluationStatistics incrementFp(int delta) {
        this.fp += delta;
        return this;
    }

    public EvaluationStatistics incrementFn(int delta) {
        this.fn += delta;
        return this;
    }

    public EvaluationStatistics incrementStats(int tpDelta, int fpDelta, int fnDelta) {
        incrementTp(tpDelta);
        incrementFp(fpDelta);
        incrementFn(fnDelta);
        return this;
    }

    public double getFMeasure() {
        return FMeasure.getFMeasure(tp, fp, fn);
    }

    public double getRecall() {
        return FMeasure.getRecall(tp, fp, fn);
    }

    public double getPrecision() {
        return FMeasure.getPrecision(tp, fp, fn);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fn;
        result = prime * result + fp;
        result = prime * result + tp;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EvaluationStatistics other = (EvaluationStatistics) obj;
        if (fn != other.fn)
            return false;
        if (fp != other.fp)
            return false;
        if (tp != other.tp)
            return false;
        return true;
    }

}
