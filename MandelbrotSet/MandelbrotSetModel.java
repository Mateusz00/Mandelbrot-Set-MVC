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
        iterationsData = new ArrayList<>(Collections.nCopies(Application.HEIGHT * Application.WIDTH, (long) 0));
        center = new Point2D.Double(-0.5, 0.1);
        zoom   = new double[]{1.7, 1.3};
        xRange = new double[]{center.getX() - zoom[0], center.getX() + zoom[0]};
        yRange = new double[]{center.getY() - zoom[1], center.getY() + zoom[1]};
        xStep  = (xRange[1] - xRange[0]) / Application.WIDTH;
        yStep  = (yRange[1] - yRange[0]) / Application.HEIGHT;
    }

    /**
     * Calculates number of iterations for every pixel of the main window
     */
    public void initialize() {
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
        double Pr = xRange[0];

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
}