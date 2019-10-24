package MandelbrotSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class MandelbrotSetView extends JPanel
{
    private MandelbrotSetControls controller;
    private MandelbrotSetModel model;
    private BufferedImage mandelbrotImg;
    private List<Long> iterationsData;

    public MandelbrotSetView(MandelbrotSetModel pModel, MandelbrotSetControls pController) {
        model = pModel;
        controller = pController;
        model.addObserver(new ModelObserver());
        mandelbrotImg = new BufferedImage(Application.WIDTH, Application.HEIGHT, BufferedImage.TYPE_INT_RGB);
        addBindings();
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
        iterationsData = model.getIterationsData();
        long maxIterations = model.getMaxIterations();

        SwingUtilities.invokeLater(() -> {
            for(int i = 0; i < iterationsData.size(); ++i) {
                float[] HSB = calculateHSB(iterationsData.get(i), maxIterations);
                mandelbrotImg.setRGB(i % Application.WIDTH, i / Application.WIDTH, Color.HSBtoRGB(HSB[0], HSB[1], HSB[2]));
            }

            repaint();
        });
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

    private void addBindings() {
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveLeft");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveRight");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveUp");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDown");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), "zoomIn");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), "zoomOut");

        getActionMap().put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.moveCenterToLeft();
            }
        });
        getActionMap().put("moveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.moveCenterToRight();
            }
        });
        getActionMap().put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.moveCenterUp();
            }
        });
        getActionMap().put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.moveCenterDown();
            }
        });
        getActionMap().put("zoomIn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.zoomIn();
            }
        });
        getActionMap().put("zoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.zoomOut();
            }
        });
    }
}
