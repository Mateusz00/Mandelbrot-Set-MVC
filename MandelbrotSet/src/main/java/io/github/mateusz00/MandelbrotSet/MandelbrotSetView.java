package io.github.mateusz00.MandelbrotSet;

import io.github.mateusz00.MandelbrotSet.RGBPickers.RGBPicker;
import io.github.mateusz00.MandelbrotSet.RGBPickers.PickerRed;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class MandelbrotSetView extends JPanel
{
    private MandelbrotSetControls controller;
    private MandelbrotSetModel model;
    final private BufferedImage mandelbrotImg;
    private List<Long> iterationsData;
    private long currentMaxIterations = 0;
    private Dimension currentSize;
    private RGBPicker colorPicker = new PickerRed();

    public MandelbrotSetView(MandelbrotSetModel pModel, MandelbrotSetControls pController) {
        model = pModel;
        controller = pController;
        model.addObserver(new ModelObserver());
        currentSize = model.getSize();
        setPreferredSize(currentSize);
        mandelbrotImg = new BufferedImage(currentSize.width, currentSize.height, BufferedImage.TYPE_INT_RGB);
        addBindings();
    }

    private class ModelObserver implements Observer
    {
        @Override
        public void update(Observable obs, Object obj) {
            iterationsData = model.getIterationsData();
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
        for(int i = 0; i < iterationsData.size(); ++i) {
            int color = colorPicker.iterationsToRGB(iterationsData.get(i), currentMaxIterations);
            mandelbrotImg.setRGB(i % currentSize.width, i / currentSize.width, color);
        }
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
}
