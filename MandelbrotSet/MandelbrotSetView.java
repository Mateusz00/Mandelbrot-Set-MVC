package MandelbrotSet;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class MandelbrotSetView extends JPanel
{
    private MandelbrotSetModel model;
    private BufferedImage mandelbrotImg;
    private List<Long> iterationsData;

    public MandelbrotSetView(MandelbrotSetModel pModel) {
        model = pModel;
        model.addObserver(new ModelObserver());
        iterationsData = model.getIterationsData();
        mandelbrotImg = new BufferedImage(Application.WIDTH, Application.HEIGHT, BufferedImage.TYPE_INT_RGB);
    }

    private class ModelObserver implements Observer
    {
        @Override
        public void update(Observable obs, Object obj) {
            updateView();
        }
    }

    /**
     * Assigns RGB values to each pixel based on number of iterations for point representing that pixel
     */
    private void updateView() {
        long maxIterations = model.getMaxIterations();

        for(int i = 0; i < iterationsData.size(); ++i) {
            float[] HSB = calculateHSB(iterationsData.get(i), maxIterations);
            mandelbrotImg.setRGB(i % Application.WIDTH, i / Application.WIDTH, Color.HSBtoRGB(HSB[0], HSB[1], HSB[2]));
        }

        repaint();
    }

    /**
     * @return float array with 3 values (0 - Hue, 1 - Saturation, 2 - Brightness)
     */
    private float[] calculateHSB(long iterations, long maxIterations) {
        if(iterations == maxIterations)
            return new float[]{0, 0, 0}; // Black in HSB

        float h = ((float)iterations / maxIterations), s = 1, b = 1;

        return new float[]{h, s, b};
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(mandelbrotImg, 0, 0, null);
    }
}
