package MandelbrotSet;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

class MandelbrotSetModel extends Observable
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

    public MandelbrotSetModel() {
        iterationsData = new ArrayList<>(Collections.nCopies(Application.HEIGHT * Application.WIDTH, 0L));
        center = new Point2D.Double(-0.5, 0.1);
        zoom   = new double[]{DEFAULT_ZOOM_X, DEFAULT_ZOOM_Y};
        calculateRange();
        calculateStep();
    }

    /**
     * Calculates number of iterations for every pixel of the main window
     */
    void generate() {
        generateConcurrently(0, Application.WIDTH, 0, Application.HEIGHT);
    }

    private void generateConcurrently(int firstPixel, int lastPixel, int firstLine, int lastLine) {
        synchronized(iterationsLock) { synchronized(iterationsData) {
            ForkGenerate fg = new ForkGenerate(firstPixel, lastPixel, firstLine, lastLine);
            pool.invoke(fg);

            setChanged();
            notifyObservers();
        } }
    }

    private void generateBlock(int firstPixel, int lastPixel, int firstLine, int lastLine) {
        for(int y = firstLine; y < lastLine; ++y)
            generateLine(firstPixel, lastPixel, y);
    }

    /**
     * Calculates number of iterations for every pixel in range <firstPixel, lastPixel)
     * @param firstPixel inclusive
     * @param lastPixel exclusive
     */
    private void generateLine(int firstPixel, int lastPixel, int line) {
        double Pi = yRange[0] + yStep*line;
        double Pr = xRange[0] + xStep*firstPixel;

        for(int i = firstPixel; i < lastPixel; ++i, Pr += xStep)
            iterationsData.set(line * Application.WIDTH + i, getIterations(Pr, Pi));
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

    public void moveCenter(Point2D dir, int pixels) {
        synchronized(stepLock) { synchronized(rangeLock) {
            Utility.normalizeDirectionVector(dir);
            double xChange = xStep * pixels;
            double yChange = yStep * pixels;
            Point2D changeVector = new Point2D.Double(xChange * dir.getX(), yChange * dir.getY());

            center.setLocation(center.getX() + changeVector.getX(), center.getY() + changeVector.getY());
            calculateRange();
            moveMandelbrotSet(changeVector);
        } }
    }

    private void moveMandelbrotSet(Point2D changeVector) {
        int xShift = (int) (changeVector.getX() / xStep);
        int yShift = (int) (changeVector.getY() / yStep);

        // Generate new set if nothing can be shifted
        if(Math.abs(xShift) >= Application.WIDTH || Math.abs(yShift) >= Application.HEIGHT) {
            generate();
            return;
        }

        // Shift left/right (If center moved to the right then shift data to the left)
        if(xShift != 0) {
            int shiftRangeBeg = (xShift > 0) ? xShift : (Application.WIDTH - 1 + xShift);
            int direction = xShift / Math.abs(xShift); // Increment when moving center to the right, decrement otherwise
            int shiftRangeEnd = (xShift > 0) ? Application.WIDTH : -1;

            // Shifts array data by xShift and fills emptied cells
            synchronized(iterationsLock) { synchronized(iterationsData) {
                for(int y = 0; y < Application.HEIGHT; ++y) {
                    int lineOffset = y * Application.WIDTH;

                    for(int i = shiftRangeBeg; i != shiftRangeEnd; i += direction)
                        iterationsData.set(i - xShift + lineOffset, iterationsData.get(i + lineOffset));
                }
            } }

            if(xShift > 0)
                generateConcurrently(Application.WIDTH - 1 - xShift, Application.WIDTH, 0, Application.HEIGHT);
            else
                generateConcurrently(0, -xShift, 0, Application.HEIGHT);
        }

        // Shift up/down (If center moved down then shift data upwards)
        if(yShift != 0) {
            int shiftRangeBeg = (yShift > 0) ? yShift : (Application.HEIGHT - 1 + yShift);
            int direction = yShift / Math.abs(yShift); // Increment when moving center down, decrement otherwise
            int shiftRangeEnd = (yShift > 0) ? Application.HEIGHT : -1;

            // Shifts array data by yShift and fills emptied cells
            synchronized(iterationsLock) { synchronized(iterationsData) {
                for(int y = shiftRangeBeg; y != shiftRangeEnd; y += direction) {
                    int lineOffset = y * Application.WIDTH;

                    for(int i = 0; i < Application.WIDTH; ++i)
                        iterationsData.set(i + lineOffset - yShift * Application.WIDTH, iterationsData.get(i + lineOffset));
                }
            } }

            // Fill cells that hold invalid data
            if(yShift > 0)
                generateConcurrently(0, Application.WIDTH, (Application.HEIGHT - yShift), Application.HEIGHT);
            else
                generateConcurrently(0, Application.WIDTH, 0, -yShift);
        }
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

    private void calculateRange() {
        synchronized(rangeLock) {
            xRange = new double[]{center.getX() - zoom[0], center.getX() + zoom[0]};
            yRange = new double[]{center.getY() - zoom[1], center.getY() + zoom[1]};
        }
    }

    private void calculateStep() {
        synchronized(stepLock) {
            xStep = getXRange() / Application.WIDTH;
            yStep = getYRange() / Application.HEIGHT;
        }
    }

    void zoom(float zoomChange) {
        synchronized(stepLock) { synchronized(rangeLock) { synchronized(zoom) {
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