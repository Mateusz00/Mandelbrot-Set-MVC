package io.github.mateusz00.MandelbrotSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.function.Consumer;

public class RectangleSelector
{
    private JPanel drawingSurface;
    private Consumer<MouseEvent> onRelease;
    private Point firstCorner = new Point();
    private Rectangle rectangle = new Rectangle();
    private boolean isActive = false;
    private double ratio = -1;

    public RectangleSelector(JPanel drawingSurface) {
        this.drawingSurface = drawingSurface;

        drawingSurface.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3) {
                    isActive = true;
                    firstCorner.setLocation(e.getPoint());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3 && isActive) {
                    if(onRelease != null)
                        onRelease.accept(e);

                    isActive = false;
                    getDrawingSurface().repaint();
                }
            }
        });

        drawingSurface.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(isActive) {
                    // Set size and top-left corner of rectangle
                    rectangle.setSize(Math.abs(e.getX() - firstCorner.x), Math.abs(e.getY() - firstCorner.y));
                    rectangle.setLocation(Math.min(e.getX(), firstCorner.x), Math.min(e.getY(), firstCorner.y));
                    enforceRatio();

                    getDrawingSurface().repaint();
                }
            }
        });
    }

    public void paintComponent(Graphics graphics) {
        if(isActive) {
            Graphics2D graphics2D = (Graphics2D) graphics;

            graphics2D.setColor(new Color(223, 223, 223, 70));
            graphics2D.fill(rectangle);

            graphics2D.setColor(Color.WHITE);
            graphics2D.setStroke(new BasicStroke(1));
            graphics2D.draw(rectangle);
        }
    }

    public void setOnReleaseAction(Consumer<MouseEvent> onRelease) {
        this.onRelease = onRelease;
    }

    public double getRatio() {
        return ratio;
    }

    /**
     * Sets selection ratio
     * @param ratio width : height ratio
     */
    public void setRatio(double ratio) {
        if(ratio > 0)
            this.ratio = ratio;
        else
            throw new RuntimeException("Ratio can't be zero or less");
    }

    private void enforceRatio() {
        if(ratio > 0) {
            // Sets current height or height calculated from ratio as current height (chooses bigger one)
            rectangle.setSize(rectangle.width, (int) (Math.max(rectangle.getWidth() / ratio, rectangle.getHeight())));

            // Sets current width or width calculated from ratio as current width (chooses bigger one)
            rectangle.setSize((int) (Math.max(rectangle.getHeight() * ratio, rectangle.getWidth())), rectangle.height);

            // Fix location (firstCorner have to be one of the corners)
            int xDifference = Math.abs(firstCorner.x - rectangle.x);
            int yDifference = Math.abs(firstCorner.y - rectangle.y);

            if(xDifference != 0 && xDifference != rectangle.width)
                rectangle.setLocation(rectangle.x + xDifference - rectangle.width, rectangle.y);
            if(yDifference != 0 && yDifference != rectangle.height)
                rectangle.setLocation(rectangle.x, rectangle.y + yDifference - rectangle.height);
        }
    }

    public Point getLocation() {
        return rectangle.getLocation();
    }

    public Dimension getSize() {
        return rectangle.getSize();
    }

    public JPanel getDrawingSurface() {
        return drawingSurface;
    }
}
