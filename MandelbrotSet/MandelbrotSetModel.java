package MandelbrotSet;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MandelbrotSetModel extends Observable
{
    private final ArrayList<Long> iterationsData;
    private final Object rangeLock = new Object();
    private final Object stepLock = new Object();
    private final Object iterationsLock = new Object();
    private static final long DEFAULT_MAX_ITERATIONS = 250;
    private static final double DEFAULT_ZOOM_X = 1.7;
    private static final double DEFAULT_ZOOM_Y = 1.3;
    private long maxIterations = DEFAULT_MAX_ITERATIONS;
    private long escapeRadius = 4;
    private final double[] zoom;
    private double zoomPercent = 0;
    private double[] xRange;
    private double[] yRange;
    private Point2D.Double center;
    private double yStep;
    private double xStep;
    private static int THRESHOLD_Y = 50;
    private ForkJoinPool pool = ForkJoinPool.commonPool();
    private int mWidth;
    private int mHeight;

    /**
     * @param size Mandelbrot set size
     */
    public MandelbrotSetModel(Dimension size) {
        mWidth = size.width;
        mHeight = size.height;
        iterationsData = new ArrayList<>(Collections.nCopies(mHeight * mWidth, 0L));
        center = new Point2D.Double(-0.5, 0.1);
        zoom = new double[]{DEFAULT_ZOOM_X, DEFAULT_ZOOM_Y};
        
        calculateRange();
        calculateStep();
    }

    /**
     * Calculates number of iterations for every pixel of the main window
     */
    public void generate() {
        generateConcurrently(0, mWidth, 0, mHeight);
    }

    private void generateConcurrently(int startX, int endX, int startY, int endY) {
        generateConcurrently(startX, endX, startY, endY, true);
    }

    private void generateConcurrently(int startX, int endX, int startY, int endY, boolean showResult) {
        // Ensure that whole set will be generated using the same data
        synchronized(rangeLock) { synchronized(iterationsLock) { synchronized(iterationsData) { synchronized(stepLock) {
            ForkGenerate fg = new ForkGenerate(startX, endX, startY, endY);
            pool.invoke(fg);

            if(showResult) {
                setChanged();
                notifyObservers();
            }
        } } } }
    }

    private void generateBlock(int firstPixel, int lastPixel, int firstLine, int lastLine) {
        for(int y = firstLine; y < lastLine; ++y)
            generateLine(firstPixel, lastPixel, y);
    }

    /**
     * Calculates number of iterations for every pixel in range &lt;firstPixel, lastPixel)
     * @param firstPixel inclusive
     * @param lastPixel exclusive
     * @param line line
     */
    private void generateLine(int firstPixel, int lastPixel, int line) {
        double Pi = yRange[0] + yStep*line;
        double Pr = xRange[0] + xStep*firstPixel;

        for(int i = firstPixel; i < lastPixel; ++i, Pr += xStep)
            iterationsData.set(line * mWidth + i, getIterations(Pr, Pi));
    }

    /**
     * @param Pr real part of point
     * @param Pi imaginary part of point
     * @return Number of iterations for given point until going past escapeRadius
     */
    private long getIterations(double Pr, double Pi) {
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

        return n;
    }

    /**
     * @return Array containing calculated iterations for each pixel
     */
    public List<Long> getIterationsData() {
        synchronized(iterationsData) {
            ArrayList<Long> copy = new ArrayList<>(iterationsData.size());

            for(long data : iterationsData)
                copy.add(data);

            return copy;
        }
    }

    public long getMaxIterations() {
        return maxIterations;
    }

    public Dimension getSize() {
        return new Dimension(mWidth, mHeight);
    }

    /**
     * @param changeVector which direction and how far(in pixels) will center be moved.
     */
    public void moveCenter(Point changeVector) {
        synchronized(rangeLock) { synchronized(stepLock) {  synchronized(zoom) {
            double xChange = xStep * changeVector.getX();
            double yChange = yStep * changeVector.getY();

            center.setLocation(center.getX() + xChange, center.getY() + yChange);
            calculateRange();
            moveMandelbrotSet(changeVector);
        } } }
    }

    private void moveMandelbrotSet(Point changeVectorPixels) {
        int xShift = changeVectorPixels.x;
        int yShift = changeVectorPixels.y;

        // Generate new set if nothing can be shifted
        if(Math.abs(xShift) >= mWidth || Math.abs(yShift) >= mHeight) {
            generate();
            return;
        }

        // Fields defining the area where data have to be generated as it holds invalid values
        int xStart = 0, xEnd = 0, yStart = 0, yEnd = 0;
        int xStartLine=0, xEndLine = mHeight, yStartPixel = 0, yEndPixel = mWidth;

        // Shift left/right (If center moved to the right then shift data to the left)
        if(xShift != 0) {
            int shiftRangeBeg = (xShift > 0) ? xShift : (mWidth - 1 + xShift);
            int direction = xShift / Math.abs(xShift); // Increment when moving center to the right, decrement otherwise
            int shiftRangeEnd = (xShift > 0) ? mWidth : -1;

            // Shifts array data by xShift and fills emptied cells
            synchronized(iterationsLock) { synchronized(iterationsData) {
                for(int y = 0; y < mHeight; ++y) {
                    int lineOffset = y * mWidth;

                    for(int i = shiftRangeBeg; i != shiftRangeEnd; i += direction)
                        iterationsData.set(i - xShift + lineOffset, iterationsData.get(i + lineOffset));
                }
            } }

            // Defines the area where data have to be generated as it holds invalid values
            if(xShift > 0) {
                xStart = mWidth - xShift;
                xEnd = mWidth;
            }
            else {
                xStart = 0;
                xEnd = -xShift;
            }
        }

        // Shift up/down (If center moved down then shift data upwards)
        if(yShift != 0) {
            int shiftRangeBeg = (yShift > 0) ? yShift : (mHeight - 1 + yShift);
            int direction = yShift / Math.abs(yShift); // Increment when moving center down, decrement otherwise
            int shiftRangeEnd = (yShift > 0) ? mHeight : -1;

            // Shifts array data by yShift and fills emptied cells
            synchronized(iterationsLock) { synchronized(iterationsData) {
                for(int y = shiftRangeBeg; y != shiftRangeEnd; y += direction) {
                    int lineOffset = y * mWidth;

                    for(int i = 0; i < mWidth; ++i)
                        iterationsData.set(i + lineOffset - yShift * mWidth, iterationsData.get(i + lineOffset));
                }
            } }

            // Defines the area where data have to be generated as it holds invalid values
            if(yShift > 0) {
                yStart = mHeight - yShift;
                yEnd = mHeight;
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

    private double getXRange() {
        synchronized(rangeLock) {
            return (xRange[1] - xRange[0]);
        }
    }

    private double getYRange() {
        synchronized(rangeLock) {
            return (yRange[1] - yRange[0]);
        }
    }

    public Point2D.Double getCenter() {
        return center;
    }

    private void calculateRange() {
        synchronized(rangeLock) { synchronized(zoom) {
            xRange = new double[]{center.getX() - zoom[0], center.getX() + zoom[0]};
            yRange = new double[]{center.getY() - zoom[1], center.getY() + zoom[1]};
        } }
    }

    private void calculateStep() {
        synchronized(rangeLock) { synchronized(stepLock) {
            xStep = getXRange() / mWidth;
            yStep = getYRange() / mHeight;
        } }
    }

    public void zoom(float zoomChange) {
        synchronized(rangeLock) { synchronized(zoom) { synchronized(stepLock) {
            zoom[0] *= (1 - zoomChange);
            zoom[1] *= (1 - zoomChange);

            zoomPercent += zoomChange;
            maxIterations = (zoomPercent > 0) ? (long) (DEFAULT_MAX_ITERATIONS * (1 + zoomPercent)) : DEFAULT_MAX_ITERATIONS;

            calculateRange();
            calculateStep();
            generate();
        } } }
    }

    private class ForkGenerate extends RecursiveAction
    {
        private int mStartX;
        private int mEndX;
        private int mStartY;
        private int mEndY;

        public ForkGenerate(int startX, int endX, int startY, int endY) {
            mStartX = startX;
            mEndX = endX;
            mStartY = startY;
            mEndY = endY;
        }

        @Override
        protected void compute() {
            int length = mEndY - mStartY;

            if(length < THRESHOLD_Y) {
                generateBlock(mStartX, mEndX, mStartY, mEndY);
            }
            else {
                int mid = length / 2;

                invokeAll(new ForkGenerate(mStartX, mEndX, mStartY, mStartY + mid),
                          new ForkGenerate(mStartX, mEndX, mStartY + mid, mEndY));
            }
        }
    }
}