package io.github.mateusz00.MandelbrotSet.RGBPickers;

import io.github.mateusz00.MandelbrotSet.MandelbrotSetResult;

import java.awt.*;

public class PickerBlue implements RGBPicker
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

        Color rgb = new Color(Color.HSBtoRGB(h, 1, b));
        Color rgb2 = new Color(rgb.getBlue(), rgb.getGreen(), rgb.getRed()); // Swap red with blue

        return rgb2.getRGB();
    }

    @Override
    public String getDescription() {
        return "Dark blue";
    }
}
