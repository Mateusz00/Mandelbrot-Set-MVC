package MandelbrotSet;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

class MandelbrotSetModel extends Observable
{
    private ArrayList<Long> iterationsData;
    private long maxIterations = 200;
    private long escapeRadius = 4;
    private double[] zoom;
    private double[] xRange;
    private double[] yRange;
    private Point2D.Double center;
    private double yStep;
    private double xStep;

    public MandelbrotSetModel() {
        iterationsData = new ArrayList<>(Collections.nCopies(Application.HEIGHT * Application.WIDTH, 0L));
        center = new Point2D.Double(-0.5, 0.1);
        zoom   = new double[]{1.7, 1.3};
        calculateRange();
        calculateStep();
    }

    /**
     * Calculates number of iterations for every pixel of the main window
     */
    public void generate() {
        for(int y=0; y < Application.HEIGHT; ++y)
            generateLine(0, Application.WIDTH, y);

        setChanged();
        notifyObservers();
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
        return Collections.unmodifiableList(iterationsData);
    }

    public long getMaxIterations() {
        return maxIterations;
    }

    public void moveCenter(Point2D dir, int pixels) {
        Utility.normalizeDirectionVector(dir);
        double xChange = xStep * pixels;
        double yChange = yStep * pixels;
        Point2D changeVector = new Point2D.Double(xChange * dir.getX(), yChange * dir.getY());

        center.setLocation(center.getX() + changeVector.getX(), center.getY() + changeVector.getY());
        calculateRange();
        moveMandelbrotSet(changeVector);
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
            for(int y = 0; y < Application.HEIGHT; ++y) {
                int lineOffset = y * Application.WIDTH;

                for(int i = shiftRangeBeg; i != shiftRangeEnd; i += direction)
                    iterationsData.set(i - xShift + lineOffset, iterationsData.get(i + lineOffset));

                if(xShift > 0)
                    generateLine(Application.WIDTH - 1 - xShift, Application.WIDTH, y);
                else
                    generateLine(0, -xShift, y);
            }
        }
        // Shift up/down (If center moved down then shift data upwards)
        if(yShift != 0) {
            int shiftRangeBeg = (yShift > 0) ? yShift : (Application.HEIGHT - 1 + yShift);
            int direction = yShift / Math.abs(yShift); // Increment when moving center down, decrement otherwise
            int shiftRangeEnd = (yShift > 0) ? Application.HEIGHT : -1;

            // Shifts array data by yShift and fills emptied cells
            for(int y = shiftRangeBeg; y != shiftRangeEnd; y += direction) {
                int lineOffset = y * Application.WIDTH;

                for(int i = 0; i < Application.WIDTH; ++i)
                    iterationsData.set(i + lineOffset - yShift * Application.WIDTH, iterationsData.get(i + lineOffset));
            }

            // Fill cells that hold invalid data
            for(int y = Application.HEIGHT - 1 - shiftRangeBeg; y != shiftRangeEnd; y += direction)
                generateLine(0, Application.WIDTH, y);
        }

        setChanged();
        notifyObservers();
    }
    
    private double getXRange() {
        return (xRange[1] - xRange[0]);
    }

    private double getYRange() {
        return (yRange[1] - yRange[0]);
    }

    private void calculateRange() {
        xRange = new double[]{center.getX() - zoom[0], center.getX() + zoom[0]};
        yRange = new double[]{center.getY() - zoom[1], center.getY() + zoom[1]};
    }

    private void calculateStep() {
        xStep  = getXRange() / Application.WIDTH;
        yStep  = getYRange() / Application.HEIGHT;
    }
}