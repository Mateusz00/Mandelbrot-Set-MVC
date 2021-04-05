package io.github.mateusz00.MandelbrotSet.RGBPickers;

import io.github.mateusz00.MandelbrotSet.mandelbrot.MandelbrotSetResult;

import java.awt.*;

public class PickerRed implements RGBPicker
{
    @Override
    public int iterationsToRGB(MandelbrotSetResult result, long maxIterations, boolean colorSmoothing) {
        if(result.getIterations() == maxIterations)
            return Color.BLACK.getRGB();

        double value = result.getIterations();
        if(colorSmoothing)
            value = (result.getIterations() + 1 - Math.log(Math.log(result.getEscapeValue())) / Math.log(2));

        return Color.HSBtoRGB((float) (value / maxIterations), 1, 1);
    }

    @Override
    public String getDescription() {
        return "Red";
    }
}
