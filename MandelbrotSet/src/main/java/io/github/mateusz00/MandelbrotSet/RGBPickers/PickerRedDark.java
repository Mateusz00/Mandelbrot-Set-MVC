package io.github.mateusz00.MandelbrotSet.RGBPickers;

import io.github.mateusz00.MandelbrotSet.MandelbrotSetResult;

import java.awt.*;

public class PickerRedDark implements RGBPicker
{
    @Override
    public int iterationsToRGB(MandelbrotSetResult result, long maxIterations, boolean colorSmoothing) {
        if(result.getIterations() == maxIterations)
            return Color.BLACK.getRGB();

        double value = result.getIterations();
        if(colorSmoothing)
            value = (result.getIterations() + 1 - Math.log(Math.log(result.getEscapeValue())) / Math.log(2));

        float h = ((float) (value / maxIterations));
        float b = ((float) (value / maxIterations)) * 9.5f;
        b = Math.min(1, b);

        return Color.HSBtoRGB(h, 1, b);
    }

    @Override
    public String getDescription() {
        return "Dark red";
    }
}
