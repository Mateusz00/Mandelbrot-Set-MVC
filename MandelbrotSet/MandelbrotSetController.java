package MandelbrotSet;

import java.awt.*;
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

    private void tryMoving(Point changeVector) {
        // Ensures that threads won't queue (makes app more responsive)
        if(!moveLock.isLocked()) {
            new Thread(() -> {
                if(moveLock.tryLock()) {
                    try {
                        model.moveCenter(changeVector);
                    }
                    finally {
                        moveLock.unlock();
                    }
                }
            }).start();
        }
    }

    private void tryZooming(float zoomPercent) {
        // Ensures that threads won't queue (makes app more responsive)
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
        tryMoving(new Point(-MOVE_PIXELS_X, 0));
    }

    @Override
    public void moveCenterToRight() {
        tryMoving(new Point(MOVE_PIXELS_X, 0));
    }

    @Override
    public void moveCenterUp() {
        tryMoving(new Point(0, -MOVE_PIXELS_Y));
    }

    @Override
    public void moveCenterDown() {
        tryMoving(new Point(0, MOVE_PIXELS_Y));
    }

    @Override
    public void moveCenterTo(Point point) {
        Point center = new Point(Application.WIDTH / 2, Application.HEIGHT / 2);
        Point change = new Point(point.x - center.x, point.y - center.y);

        if(Utility.vectorLength(change) > 0)
            tryMoving(change);
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
