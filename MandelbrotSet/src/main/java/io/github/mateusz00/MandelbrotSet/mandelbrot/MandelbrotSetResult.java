package io.github.mateusz00.MandelbrotSet.mandelbrot;

public class MandelbrotSetResult
{
    private long iterations;
    private double escapeValue;

    public MandelbrotSetResult(long iterations, double escapeValue) {
        this.iterations = iterations;
        this.escapeValue = escapeValue;
    }

    public long getIterations() {
        return iterations;
    }

    public double getEscapeValue() {
        return escapeValue;
    }
}
