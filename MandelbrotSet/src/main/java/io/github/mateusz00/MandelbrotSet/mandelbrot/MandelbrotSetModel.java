package io.github.mateusz00.MandelbrotSet.mandelbrot;

import java.awt.*;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MandelbrotSetModel extends Observable
{
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private ArrayList<MandelbrotSetResult> results;
    private static final long DEFAULT_MAX_ITERATIONS = 250;
    private static final long DEFAULT_ESCAPE_RADIUS = 40;
    private static final double DEFAULT_X_RANGE = 3.4;
    private static final double DEFAULT_Y_RANGE = 2.6;
    private static final double DEFAULT_CENTER_X = -0.5;
    private static final double DEFAULT_CENTER_Y = 0.1;
    private long maxIterations = DEFAULT_MAX_ITERATIONS;
    private long escapeRadius = DEFAULT_ESCAPE_RADIUS;
    private double xRange = DEFAULT_X_RANGE;
    private double yRange = DEFAULT_Y_RANGE;
    private Point2D.Double center;
    private double yStep;
    private double xStep;
    private static int THRESHOLD_Y = 50;
    private double maxIterationsMultiplier = 1;
    private ForkJoinPool pool = ForkJoinPool.commonPool();
    private Dimension size;

    /**
     * @param size Mandelbrot set size
     */
    public MandelbrotSetModel(Dimension size) {
        this.size = size;
        results = new ArrayList<>(Collections.nCopies(size.width * size.height, new MandelbrotSetResult(0, 0)));
        center = new Point2D.Double(DEFAULT_CENTER_X, DEFAULT_CENTER_Y);

        calculateStep();
    }

    /**
     * Calculates MandelbrotSetResult for every pixel of the main window
     */
    public void generate() {
        generateConcurrently(0, size.width, 0, size.height);
    }

    private void generateConcurrently(int startX, int endX, int startY, int endY) {
        generateConcurrently(startX, endX, startY, endY, true);
    }

    private synchronized void generateConcurrently(int startX, int endX, int startY, int endY, boolean showResult) {
        ForkGenerate fg = new ForkGenerate(startX, endX, startY, endY);
        pool.invoke(fg);

        if(showResult) {
            setChanged();
            notifyObservers();
        }
    }

    private void generateBlock(int firstPixel, int lastPixel, int firstLine, int lastLine) {
        for(int y = firstLine; y < lastLine; ++y)
            generateLine(firstPixel, lastPixel, y);
    }

    /**
     * Calculates MandelbrotSetResult for every pixel in range &lt;firstPixel, lastPixel)
     * @param firstPixel inclusive
     * @param lastPixel exclusive
     * @param line line
     */
    private void generateLine(int firstPixel, int lastPixel, int line) {
        double Pi = (center.y - yRange / 2.0) + yStep * line;
        double Pr = (center.x - xRange / 2.0) + xStep * firstPixel;

        for(int i = firstPixel; i < lastPixel; ++i, Pr += xStep)
            results.set(line * size.width + i, getIterations(Pr, Pi));
    }

    /**
     * @param Pr real part of point
     * @param Pi imaginary part of point
     * @return MandelbrotSetResult for given point until going past escapeRadius
     */
    private MandelbrotSetResult getIterations(double Pr, double Pi) {
        double Zr  = 0;
        double Zi  = 0;
        double Zr2 = 0; // decreases amount of multiplications
        double Zi2 = 0; // decreases amount of multiplications
        long   n   = 0;

        // Mandelbrot set equation
        for( ; n < maxIterations && Zr2+Zi2 <= escapeRadius; ++n) {
            Zi = 2 * Zr * Zi + Pi;
            Zr = Zr2 - Zi2 + Pr;
            Zr2 = Zr * Zr;
            Zi2 = Zi * Zi;
        }

        return new MandelbrotSetResult(n, (Zr2+Zi2));
    }

    /**
     * @return Array containing calculated MandelbrotSetResult for each pixel
     */
    public synchronized List<MandelbrotSetResult> getResults() {
        ArrayList<MandelbrotSetResult> copy = new ArrayList<>(results.size());

        for(MandelbrotSetResult result : results)
            copy.add(new MandelbrotSetResult(result.getIterations(), result.getEscapeValue()));

        return copy;
    }

    /**
     * @param changeVector which direction and how far(in pixels) will center be moved.
     */
    public synchronized void moveCenter(Point changeVector) {
        double xChange = xStep * changeVector.getX();
        double yChange = yStep * changeVector.getY();

        center.setLocation(center.getX() + xChange, center.getY() + yChange);
        moveMandelbrotSet(changeVector);
    }

    private synchronized void moveMandelbrotSet(Point changeVectorPixels) {
        int xShift = changeVectorPixels.x;
        int yShift = changeVectorPixels.y;

        // Generate new set if nothing can be shifted
        if(Math.abs(xShift) >= size.width || Math.abs(yShift) >= size.height) {
            generate();
            return;
        }

        // Fields defining the area where data have to be generated as it holds invalid values
        int xStart = 0, xEnd = 0, yStart = 0, yEnd = 0;
        int xStartLine=0, xEndLine = size.height, yStartPixel = 0, yEndPixel = size.width;

        // Shift left/right (If center moved to the right then shift data to the left)
        if(xShift != 0) {
            int shiftRangeBeg = (xShift > 0) ? xShift : (size.width - 1 + xShift);
            int direction = xShift / Math.abs(xShift); // Increment when moving center to the right, decrement otherwise
            int shiftRangeEnd = (xShift > 0) ? size.width : -1;

            // Shifts array data by xShift and fills emptied cells
            for(int y = 0; y < size.height; ++y) {
                int lineOffset = y * size.width;

                for(int i = shiftRangeBeg; i != shiftRangeEnd; i += direction)
                    results.set(i - xShift + lineOffset, results.get(i + lineOffset));
            }

            // Defines the area where data have to be generated as it holds invalid values
            if(xShift > 0) {
                xStart = size.width - xShift;
                xEnd = size.width;
            }
            else {
                xStart = 0;
                xEnd = -xShift;
            }
        }

        // Shift up/down (If center moved down then shift data upwards)
        if(yShift != 0) {
            int shiftRangeBeg = (yShift > 0) ? yShift : (size.height - 1 + yShift);
            int direction = yShift / Math.abs(yShift); // Increment when moving center down, decrement otherwise
            int shiftRangeEnd = (yShift > 0) ? size.height : -1;

            // Shifts array data by yShift and fills emptied cells
            for(int y = shiftRangeBeg; y != shiftRangeEnd; y += direction) {
                int lineOffset = y * size.width;

                for(int i = 0; i < size.width; ++i)
                    results.set(i + lineOffset - yShift * size.width, results.get(i + lineOffset));
            }

            // Defines the area where data have to be generated as it holds invalid values
            if(yShift > 0) {
                yStart = size.height - yShift;
                yEnd = size.height;
            }
            else {
                yStart = 0;
                yEnd = -yShift;
            }
        }

        // If data was shifted both vertically and horizontally then generate common area only once
        if(xShift != 0 && yShift != 0) {
            // Change area to generate to avoid generating same area twice
            if(yShift > 0)
                xEndLine = yStart;
            else
                xStartLine = yEnd;

            // Change area to generate to avoid generating same area twice
            if(xShift > 0)
                yEndPixel = xStart;
            else
                yStartPixel = xEnd;

            // Generate common area
            generateConcurrently(xStart, xEnd, yStart, yEnd, false);
        }

        // Fill cells that hold invalid data
        if(xShift != 0)
            generateConcurrently(xStart, xEnd, xStartLine, xEndLine, false);

        if(yShift != 0)
            generateConcurrently(yStartPixel, yEndPixel, yStart, yEnd, false);

        // Update view
        setChanged();
        notifyObservers();
    }

    public synchronized double getXRange() {
        return xRange;
    }

    public synchronized void setXRange(double range) {
        xRange = range;
        calculateStep();
    }

    public synchronized double getYRange() {
        return yRange;
    }

    public synchronized void setYRange(double range) {
        yRange = range;
        calculateStep();
    }

    private synchronized void calculateStep() {
        xStep = getXRange() / size.width;
        yStep = getYRange() / size.height;
    }

    public synchronized void zoom(double zoomChange) {
        xRange /= zoomChange;
        yRange /= zoomChange;

        // Calculate new max iterations value
        double newIterations = DEFAULT_MAX_ITERATIONS * (Math.log(3.4 / getXRange()) + 0.5);
        long temp = Math.max((long) newIterations, DEFAULT_MAX_ITERATIONS);
        maxIterations = (long) (temp * maxIterationsMultiplier);

        calculateStep();
        generate();
    }

    public void setSize(Dimension size) {
        // If size didn't change then don't do anything
        if(this.size.width != size.width || this.size.height != size.height) {
            propertyChangeSupport.firePropertyChange("size", this.size, size);
            this.size = size;
            results = new ArrayList<>(Collections.nCopies(size.width * size.height, new MandelbrotSetResult(0, 0)));
            calculateStep();
            generate();
        }
    }

    public Dimension getSize() {
        return size;
    }

    public synchronized void setMaxIterations(long maxIterations) {
        this.maxIterations = maxIterations;
    }

    public synchronized long getMaxIterations() {
        return maxIterations;
    }

    public synchronized Point2D.Double getCenter() {
        return center;
    }

    public synchronized void setCenter(Point2D.Double center) {
        this.center = center;
    }

    public long getEscapeRadius() {
        return escapeRadius;
    }

    public synchronized void setEscapeRadius(long escapeRadius) {
        this.escapeRadius = escapeRadius;
    }

    public synchronized double getMaxIterationsMultiplier() {
        return maxIterationsMultiplier;
    }

    public synchronized void setMaxIterationsMultiplier(double maxIterationsMultiplier) {
        this.maxIterationsMultiplier = maxIterationsMultiplier;
    }

    public void restoreDefaultSettings() {
        setMaxIterations(DEFAULT_MAX_ITERATIONS);
        setMaxIterationsMultiplier(1);
        setEscapeRadius(DEFAULT_ESCAPE_RADIUS);
        setCenter(new Point2D.Double(DEFAULT_CENTER_X, DEFAULT_CENTER_Y));
        setXRange(DEFAULT_X_RANGE);
        setYRange(DEFAULT_Y_RANGE);
    }

    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(property, listener);
    }

    public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(property, listener);
    }

    private class ForkGenerate extends RecursiveAction
    {
        private int startX;
        private int endX;
        private int startY;
        private int endY;

        public ForkGenerate(int startX, int endX, int startY, int endY) {
            this.startX = startX;
            this.endX = endX;
            this.startY = startY;
            this.endY = endY;
        }

        @Override
        protected void compute() {
            int length = endY - startY;

            if(length < THRESHOLD_Y) {
                generateBlock(startX, endX, startY, endY);
            }
            else {
                int mid = length / 2;

                invokeAll(new ForkGenerate(startX, endX, startY, startY + mid),
                          new ForkGenerate(startX, endX, startY + mid, endY));
            }
        }
    }
}