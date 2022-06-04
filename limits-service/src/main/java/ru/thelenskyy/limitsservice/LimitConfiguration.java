package ru.thelenskyy.limitsservice;

/**
 * DTO for REST communication
 */
public class LimitConfiguration {
    private int max;
    private int min;

    public LimitConfiguration() {
    }

    public LimitConfiguration(int min, int max) {
        super();
        this.max = max;
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }
}
