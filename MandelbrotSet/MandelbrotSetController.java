package MandelbrotSet;

import java.awt.*;
import java.awt.geom.Point2D;

public class MandelbrotSetController implements MandelbrotSetControls
{
    private static final int movePixelsX = (int) (Application.WIDTH * 0.02);
    private static final int movePixelsY = (int) (Application.HEIGHT * 0.02);
    private static final float zoomPercent = 0.07f;
    private MandelbrotSetModel model;
    private MandelbrotSetView view;
    private Container mContainer;

    public MandelbrotSetController(MandelbrotSetModel model1, Container container) {
        model = model1;
        mContainer = container;
    }

    public void setView(MandelbrotSetView view1){
        mContainer.add(view1);
        view = view1;
    }

    public void removeView(MandelbrotSetView view1) {
        if(view == view1) {
            mContainer.remove(view);
            view = null;
        }
    }

    @Override
    public void moveCenterToLeft() {
        new Thread(() -> model.moveCenter(new Point2D.Double(-1, 0), movePixelsX)).start();
    }

    @Override
    public void moveCenterToRight() {
        new Thread(() -> model.moveCenter(new Point2D.Double(1, 0), movePixelsX)).start();
    }

    @Override
    public void moveCenterUp() {
        new Thread(() -> model.moveCenter(new Point2D.Double(0, -1), movePixelsY)).start();
    }

    @Override
    public void moveCenterDown() {
        new Thread(() -> model.moveCenter(new Point2D.Double(0, 1), movePixelsY)).start();
    }

    @Override
    public void zoomIn() {
        new Thread(() -> model.zoom(zoomPercent)).start();
    }

    @Override
    public void zoomOut() {
        new Thread(() -> model.zoom(-zoomPercent)).start();
    }
}
