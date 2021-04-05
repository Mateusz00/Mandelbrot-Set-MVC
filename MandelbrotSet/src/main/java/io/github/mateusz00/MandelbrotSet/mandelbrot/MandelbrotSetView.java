package io.github.mateusz00.MandelbrotSet.mandelbrot;

import io.github.mateusz00.MandelbrotSet.RGBPickers.PickerRed;
import io.github.mateusz00.MandelbrotSet.RGBPickers.RGBPicker;
import io.github.mateusz00.MandelbrotSet.ui.RectangleSelector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MandelbrotSetView extends JPanel
{
    private MandelbrotSetController controller;
    private MandelbrotSetModel model;
    private BufferedImage mandelbrotImg;
    private List<MandelbrotSetResult> results;
    private long currentMaxIterations = 0;
    private Dimension currentSize;
    private RGBPicker colorPicker = new PickerRed();
    private boolean smoothColoring = true;
    private final RectangleSelector rectangleSelector = new RectangleSelector(this);

    public MandelbrotSetView(MandelbrotSetModel model, MandelbrotSetController controller) {
        this.model = model;
        this.controller = controller;
        currentSize = model.getSize();
        setPreferredSize(currentSize);
        mandelbrotImg = new BufferedImage(currentSize.width, currentSize.height, BufferedImage.TYPE_INT_RGB);

        rectangleSelector.setRatio(currentSize.getWidth() / currentSize.getHeight());
        rectangleSelector.setOnReleaseAction((e) -> {
            final Dimension size = new Dimension(rectangleSelector.getSize());
            final Point location = new Point(rectangleSelector.getLocation());
            final Point center = new Point(location.x + (size.width / 2), location.y + (size.height / 2));

            new Thread(() -> {
                this.controller.moveCenterTo(center, true);
                this.controller.zoom( 1 / (size.getWidth() / currentSize.getWidth()), true);
            }).start();
        });

        model.addObserver(new ModelObserver());
        model.addPropertyChangeListener("size", (evt) -> {
            Dimension size = (Dimension) evt.getNewValue();
            mandelbrotImg = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
            rectangleSelector.setRatio(size.getWidth() / size.getHeight());
            currentSize = size;
            setPreferredSize(size);
            System.out.println(currentSize);
        });

        addBindings();
    }

    private class ModelObserver implements Observer
    {
        @Override
        public void update(Observable obs, Object obj) {
            results = model.getResults();
            currentMaxIterations = model.getMaxIterations();

            updateView();
        }
    }

    public void setColoring(RGBPicker picker) {
        colorPicker = picker;
    }

    /**
     * Assigns RGB values to each pixel based on number of iterations for point representing that pixel
     */
    public void updateView() {
        calculateColors();
        repaint();
    }

    private void calculateColors() {
        for(int i = 0; i < results.size(); ++i) {
            int color = colorPicker.iterationsToRGB(results.get(i), currentMaxIterations, smoothColoring);
            mandelbrotImg.setRGB(i % currentSize.width, i / currentSize.width, color);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(mandelbrotImg, 0, 0, null);
        rectangleSelector.paintComponent(g);
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

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1)
                    controller.moveCenterTo(e.getPoint());
            }
        });
    }

    public RGBPicker getRGBPicker() {
        return colorPicker;
    }

    public BufferedImage getBufferedImage() {
        return mandelbrotImg;
    }

    public boolean isSmoothColoringEnabled() {
        return smoothColoring;
    }

    public void setSmoothColoring(boolean flag) {
        smoothColoring = flag;
    }

    public void restoreDefaultSettings() {
        setSmoothColoring(true);
    }
}
