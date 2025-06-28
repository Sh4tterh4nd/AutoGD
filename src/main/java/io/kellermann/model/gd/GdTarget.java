package io.kellermann.model.gd;

public enum GdTarget {
    PODCAST(0.8, 1.0),
    VIDEO(0.0, 0.80);

    public final double minProgress;
    public final double maxProgress;

    private GdTarget(double minProgress, double maxProgress) {
        this.minProgress = minProgress;
        this.maxProgress = maxProgress;
    }
}
