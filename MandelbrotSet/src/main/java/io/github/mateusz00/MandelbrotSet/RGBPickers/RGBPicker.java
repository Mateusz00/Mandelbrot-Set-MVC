package io.github.mateusz00.MandelbrotSet.RGBPickers;

import io.github.mateusz00.MandelbrotSet.MandelbrotSetResult;

public interface RGBPicker
{
    int iterationsToRGB(MandelbrotSetResult result, long maxIterations, boolean colorSmoothing);
    String getDescription();
}
