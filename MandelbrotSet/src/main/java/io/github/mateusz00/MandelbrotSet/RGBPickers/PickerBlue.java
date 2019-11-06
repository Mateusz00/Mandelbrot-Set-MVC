package io.github.mateusz00.MandelbrotSet.RGBPickers;

import java.awt.*;

public class PickerBlue implements RGBPicker
{
    @Override
    public int iterationsToRGB(long iterations, long maxIterations) {
        if(iterations == maxIterations)
            return Color.BLACK.getRGB();

        float h = ((float) iterations / maxIterations);
        float b = ((float) iterations / maxIterations) * 11;
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
