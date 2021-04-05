package io.github.mateusz00.MandelbrotSet.utilities;

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

    public static int digitsNumber(double value) {
        int digits = (value >= 1) ? 1 : 0;

        while((value /= 10) >= 1)
            ++digits;

        return digits;
    }

    /**
     * @return String containing fileName without extensions (name.ext.ext.ext -> name)
     */
    public static String removeExtension(String fileName) {
        int substrEnd = (fileName.indexOf('.') != -1) ? fileName.indexOf('.') : fileName.length();
        fileName = fileName.substring(0, substrEnd);

        return fileName;
    }
}
