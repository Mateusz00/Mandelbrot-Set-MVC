package io.github.mateusz00.MandelbrotSet.RGBPickers;

public interface RGBPicker
{
    int iterationsToRGB(long iterations, long maxIterations);
    String getDescription();
}
