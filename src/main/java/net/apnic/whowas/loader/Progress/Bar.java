package net.apnic.whowas.loader.Progress;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

public class Bar {
    private static final int WIDTH = 60;
    private int max, now, idx, steps;
    private final Consumer<String> reporter;

    public Bar(int max, Consumer<String> reporter) {
        this(max, 10, reporter);
    }

    public Bar(int max, int step, Consumer<String> reporter) {
        this.max = max;
        this.now = 0;
        this.idx = 0;
        this.steps = step;
        this.reporter = reporter;
    }

    public void inc() {
        if (++this.now >= idx * (max/steps)) {
            int at = WIDTH * idx / steps;
            reporter.accept("[" + StringUtils.repeat('#', at) + StringUtils.repeat('-', WIDTH - at) + "]");
            idx++;
        }
    }
}
