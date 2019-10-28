package MandelbrotSet.RGBPickers;

import java.awt.*;

public class PickerRedDark implements RGBPicker
{
    @Override
    public int iterationsToRGB(long iterations, long maxIterations) {
        if(iterations == maxIterations)
            return Color.BLACK.getRGB();

        float h = ((float) iterations / maxIterations);
        float b = ((float) iterations / maxIterations) * 9.5f;
        b = Math.min(1, b);

        return Color.HSBtoRGB(h, 1, b);
    }
}
