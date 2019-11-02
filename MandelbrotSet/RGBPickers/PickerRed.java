package MandelbrotSet.RGBPickers;

import java.awt.*;

public class PickerRed implements RGBPicker
{
    @Override
    public int iterationsToRGB(long iterations, long maxIterations) {
        if(iterations == maxIterations)
            return Color.BLACK.getRGB();

        return Color.HSBtoRGB(((float) iterations / maxIterations), 1, 1);
    }

    @Override
    public String getDescription() {
        return "Red";
    }
}
