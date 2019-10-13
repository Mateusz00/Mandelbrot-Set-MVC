package MandelbrotSet;

import java.awt.geom.Point2D;

public class Utility
{
    public static double vectorLength(Point2D vector) {
        return Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY());
    }

    public static void normalizeDirectionVector(Point2D vector) {
        if(vector.getX() == 0  && vector.getY() == 0)
            throw new RuntimeException("Vector can't have length of 0");

        double length = vectorLength(vector);
        vector.setLocation(vector.getX() / length, vector.getY() / length);
    }
}
