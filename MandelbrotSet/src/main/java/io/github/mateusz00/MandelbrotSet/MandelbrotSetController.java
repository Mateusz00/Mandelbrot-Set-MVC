package io.github.mateusz00.MandelbrotSet;

import io.github.mateusz00.MandelbrotSet.RGBPickers.RGBPicker;
import io.github.mateusz00.MandelbrotSet.Utilities.Utility;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

public class MandelbrotSetController implements MandelbrotSetControls
{
    private final int MOVE_PIXELS_X;
    private final int MOVE_PIXELS_Y;
    private final double DEFAULT_ZOOM_STEP = 1.05;
    private double zoomStep = DEFAULT_ZOOM_STEP;
    private MandelbrotSetModel model;
    private MandelbrotSetView view;
    private final ReentrantLock zoomLock = new ReentrantLock();
    private final ReentrantLock moveLock = new ReentrantLock();

    public MandelbrotSetController(MandelbrotSetModel model) {
        this.model = model;
        MOVE_PIXELS_X = (int) (model.getSize().width * 0.02);
        MOVE_PIXELS_Y = (int) (model.getSize().height * 0.02);
    }

    public void setView(MandelbrotSetView view){
        this.view = view;
    }

    public void removeView(MandelbrotSetView view) {
        if(this.view == view)
            this.view = null;
    }

    private void tryMoving(Point changeVector) {
        // Ensures that threads won't queue (makes app more responsive)
        if(!moveLock.isLocked()) {
            new Thread(() -> {
                if(moveLock.tryLock()) {
                    try {
                        model.moveCenter(changeVector);
                    }
                    finally {
                        moveLock.unlock();
                    }
                }
            }).start();
        }
    }

    private void tryZooming(double zoom) {
        // Ensures that threads won't queue (makes app more responsive)
        if(!zoomLock.isLocked()) {
            new Thread(() -> {
                if(zoomLock.tryLock()) {
                    try {
                        model.zoom(zoom);
                    }
                    finally {
                        zoomLock.unlock();
                    }
                }
            }).start();
        }
    }

    @Override
    public void moveCenterToLeft() {
        tryMoving(new Point(-MOVE_PIXELS_X, 0));
    }

    @Override
    public void moveCenterToRight() {
        tryMoving(new Point(MOVE_PIXELS_X, 0));
    }

    @Override
    public void moveCenterUp() {
        tryMoving(new Point(0, -MOVE_PIXELS_Y));
    }

    @Override
    public void moveCenterDown() {
        tryMoving(new Point(0, MOVE_PIXELS_Y));
    }

    public void moveCenterTo(Point point, boolean allowQueuing) {
        Point center = new Point(model.getSize().width / 2, model.getSize().height / 2);
        Point change = new Point(point.x - center.x, point.y - center.y);

        if(Utility.vectorLength(change) > 0) {
            if(!allowQueuing)
                tryMoving(change);
            else
                model.moveCenter(change);
        }
    }

    @Override
    public void zoomIn() {
        zoom(zoomStep, false);
    }

    /**
     * Zooms in or out depending on zoomChange value
     * @param zoom zooms in or out. Allowed range is (0, infinity>
     * @param allowQueuing enables/disables queuing of threads. Disable to make app more responsive
     */
    public void zoom(double zoom, boolean allowQueuing) {
        if(!allowQueuing)
            tryZooming(zoom);
        else
            model.zoom(zoom);
    }

    @Override
    public void zoomOut() {
        zoom(1 / zoomStep, false);
    }

    @Override
    public void moveCenterTo(Point point) {
        moveCenterTo(point, false);
    }

    public void setMaxIterations(long maxIterations) {
        model.setMaxIterations(maxIterations);
    }

    public long getMaxIterations() {
        return model.getMaxIterations();
    }

    public Point2D.Double getCenter() {
        return model.getCenter();
    }

    public void setCenter(Point2D.Double center) {
        model.setCenter(center);
    }

    public long getEscapeRadius() {
        return model.getEscapeRadius();
    }

    public void setEscapeRadius(long escapeRadius) {
        model.setEscapeRadius(escapeRadius);
    }

    /**
     * @return x axis range
     */
    public double getXRange() {
        return model.getXRange();
    }

    /**
     * @return y axis range
     */
    public double getYRange() {
        return model.getYRange();
    }

    /**
     * @param range x axis range
     */
    public void setXRange(double range) {
        model.setXRange(range);
    }

    /**
     * @param range y axis range
     */
    public void setYRange(double range) {
        model.setYRange(range);
    }

    public RGBPicker getCurrentRGBPicker() {
        return view.getRGBPicker();
    }

    public void setRGBPicker(RGBPicker rgb) {
        view.setColoring(rgb);
    }

    public void generateNewSet() {
        model.generate();
    }

    /**
     * @return buffered image with RGB representation of mandelbrot set
     */
    public BufferedImage getBufferedImage() {
        return view.getBufferedImage();
    }

    public double getZoomStep() {
        return zoomStep;
    }

    public void setZoomStep(double zoomStep) {
        this.zoomStep = zoomStep;
    }

    public double getMaxIterationsMultiplier() {
        return model.getMaxIterationsMultiplier();
    }

    public void setMaxIterationsMultiplier(double maxIterationsMultiplier) {
        model.setMaxIterationsMultiplier(maxIterationsMultiplier);
    }

    public boolean isSmoothColoringEnabled() {
        return view.isSmoothColoringEnabled();
    }

    public void setSmoothColoring(boolean flag) {
        view.setSmoothColoring(flag);
    }

    public void restoreDefaultSettings() {
        model.restoreDefaultSettings();
        view.restoreDefaultSettings();
        zoomStep = DEFAULT_ZOOM_STEP;
    }

    public void exportSettings(File file) {
        Properties save = new Properties();
        save.setProperty("maxIterations", String.valueOf(model.getMaxIterations()));
        save.setProperty("escapeRadius", String.valueOf(model.getEscapeRadius()));
        save.setProperty("centerX", String.valueOf(model.getCenter().getX()));
        save.setProperty("centerY", String.valueOf(model.getCenter().getY()));
        save.setProperty("maxIterationsMultiplier", String.valueOf(model.getMaxIterationsMultiplier()));
        save.setProperty("xRange", String.valueOf(model.getXRange()));
        save.setProperty("yRange", String.valueOf(model.getYRange()));
        save.setProperty("zoomStep", String.valueOf(zoomStep));
        save.setProperty("smoothColoring", String.valueOf(view.isSmoothColoringEnabled()));

        try {
            save.store(new FileOutputStream(file), "");
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void importSettings(File file) {
        Properties load = new Properties();

        try {
            load.load(new FileInputStream(file));
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        double centerX = Double.parseDouble(load.getProperty("centerX"));
        double centerY = Double.parseDouble(load.getProperty("centerY"));

        setZoomStep(Double.parseDouble(load.getProperty("zoomStep")));
        model.setMaxIterations(Long.parseLong(load.getProperty("maxIterations")));
        model.setEscapeRadius(Long.parseLong(load.getProperty("escapeRadius")));
        model.setMaxIterationsMultiplier(Double.parseDouble(load.getProperty("maxIterationsMultiplier")));
        model.setXRange(Double.parseDouble(load.getProperty("xRange")));
        model.setYRange(Double.parseDouble(load.getProperty("yRange")));
        model.setCenter(new Point2D.Double(centerX, centerY));
        view.setSmoothColoring(Boolean.parseBoolean(load.getProperty("smoothColoring")));
    }

    public void setMandelbrotSize(Dimension size) {
        model.setSize(size);
    }

    public Dimension getMandelbrotSize() {
        return model.getSize();
    }
}
