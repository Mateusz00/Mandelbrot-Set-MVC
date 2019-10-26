package MandelbrotSet;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.concurrent.locks.ReentrantLock;

public class MandelbrotSetController implements MandelbrotSetControls
{
    private static final int MOVE_PIXELS_X = (int) (Application.WIDTH * 0.02);
    private static final int MOVE_PIXELS_Y = (int) (Application.HEIGHT * 0.02);
    private static final float ZOOM_PERCENT = 0.07f;
    private MandelbrotSetModel model;
    private MandelbrotSetView view;
    private Container mContainer;
    private final ReentrantLock zoomLock = new ReentrantLock();
    private final ReentrantLock moveLock = new ReentrantLock();

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

    private void tryMoving(float x, float y, int pixelsChange) {
        if(!moveLock.isLocked()) {
            new Thread(() -> {
                if(moveLock.tryLock()) {
                    try {
                        model.moveCenter(new Point2D.Float(x, y), pixelsChange);
                    }
                    finally {
                        moveLock.unlock();
                    }
                }
            }).start();
        }
    }

    private void tryZooming(float zoomPercent) {
        if(!zoomLock.isLocked()) {
            new Thread(() -> {
                if(zoomLock.tryLock()) {
                    try {
                        model.zoom(zoomPercent);
                    }
                    finally {
                        zoomLock.unlock();
                    }
                }
            }).start();
        }
    }

    @Override
    public void moveCenterToLeft() {
        tryMoving(-1, 0, MOVE_PIXELS_X);
    }

    @Override
    public void moveCenterToRight() {
        tryMoving(1, 0, MOVE_PIXELS_X);
    }

    @Override
    public void moveCenterUp() {
        tryMoving(0, -1, MOVE_PIXELS_Y);
    }

    @Override
    public void moveCenterDown() {
        tryMoving(0, 1, MOVE_PIXELS_Y);
    }

    @Override
    public void zoomIn() {
        tryZooming(ZOOM_PERCENT);
    }

    @Override
    public void zoomOut() {
        tryZooming(-ZOOM_PERCENT);
    }
}
