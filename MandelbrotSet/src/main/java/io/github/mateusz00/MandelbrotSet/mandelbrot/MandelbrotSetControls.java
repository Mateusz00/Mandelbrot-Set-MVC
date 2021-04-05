package io.github.mateusz00.MandelbrotSet.mandelbrot;

import java.awt.*;

public interface MandelbrotSetControls
{
    void moveCenterToLeft();
    void moveCenterToRight();
    void moveCenterUp();
    void moveCenterDown();
    void moveCenterTo(Point point);
    void zoomIn();
    void zoomOut();
}
