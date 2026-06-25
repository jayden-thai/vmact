package edu.sjsu.vmact.util;

import java.util.Locale;

public class Stopwatch {
    private final long startNanos;

    private Stopwatch() {
        this.startNanos = System.nanoTime();
    }

    public static Stopwatch startNew() {
        return new Stopwatch();
    }

    public long elapsedMillis() {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    public String elapsedText() {
        double seconds = elapsedMillis() / 1000.0;
        return String.format(Locale.US, "%.2f seconds", seconds);
    }
}