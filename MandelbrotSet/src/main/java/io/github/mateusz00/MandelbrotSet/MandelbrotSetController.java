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
    private final double DEFAULT_ZOOM_STEP = 0.07;
    private double zoomStep = DEFAULT_ZOOM_STEP;
    private MandelbrotSetModel model;
    private MandelbrotSetView view;
    private final ReentrantLock zoomLock = new ReentrantLock();
    private final ReentrantLock moveLock = new ReentrantLock();

    public MandelbrotSetController(MandelbrotSetModel model1) {
        model = model1;
        MOVE_PIXELS_X = (int) (model.getSize().width * 0.02);
        MOVE_PIXELS_Y = (int) (model.getSize().height * 0.02);
    }

    public void setView(MandelbrotSetView view1){
        view = view1;
    }

    public void removeView(MandelbrotSetView view1) {
        if(view == view1)
            view = null;
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

    private void tryZooming(double zoomPercent) {
        // Ensures that threads won't queue (makes app more responsive)
        if(!zoomLock.isLocked()) {
            new Thread(() -> {
                if(zoomLock.tryLock()) {
                    try {
                        model.zoom(zoomPercent);
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

    @Override
    public void moveCenterTo(Point point) {
        Point center = new Point(model.getSize().width / 2, model.getSize().height / 2);
        Point change = new Point(point.x - center.x, point.y - center.y);

        if(Utility.vectorLength(change) > 0)
            tryMoving(change);
    }

    @Override
    public void zoomIn() {
        tryZooming(zoomStep);
    }

    @Override
    public void zoomOut() {
        tryZooming(-zoomStep);
    }

    /**
     * Zooms in without creating new thread
     */
    public void zoomInNoMultithreading() {
        model.zoom(zoomStep);
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
     * @return model's zoom array containing 2 values. (0 = xZoom, 1 = yZoom)
     */
    public double[] getZoom() {
        return model.getZoom();
    }

    /**
     * @param zoom array containing 2 values. (0 = xZoom, 1 = yZoom)
     */
    public void setZoom(double[] zoom) {
        model.setZoom(zoom);
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
        save.setProperty("zoomX", String.valueOf(model.getZoom()[0]));
        save.setProperty("zoomY", String.valueOf(model.getZoom()[1]));
        save.setProperty("zoomStep", String.valueOf(zoomStep));
        save.setProperty("zoomPercent", String.valueOf(model.getZoomPercent()));
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

        double zoomX = Double.parseDouble(load.getProperty("zoomX"));
        double zoomY = Double.parseDouble(load.getProperty("zoomY"));
        double centerX = Double.parseDouble(load.getProperty("centerX"));
        double centerY = Double.parseDouble(load.getProperty("centerY"));

        setZoomStep(Double.parseDouble(load.getProperty("zoomStep")));
        model.setMaxIterations(Long.parseLong(load.getProperty("maxIterations")));
        model.setEscapeRadius(Long.parseLong(load.getProperty("escapeRadius")));
        model.setMaxIterationsMultiplier(Double.parseDouble(load.getProperty("maxIterationsMultiplier")));
        model.setZoomPercent(Double.parseDouble(load.getProperty("zoomPercent")));
        model.setZoom(new double[]{zoomX, zoomY});
        model.setCenter(new Point2D.Double(centerX, centerY));
        view.setSmoothColoring(Boolean.parseBoolean(load.getProperty("smoothColoring")));
    }
}
