package MandelbrotSet;

import javax.swing.*;
import java.awt.geom.Point2D;

public class MandelbrotSetController implements MandelbrotSetControls
{
    private static final int movePixelsX = (int) (Application.WIDTH * 0.02);
    private static final int movePixelsY = (int) (Application.HEIGHT * 0.02);
    private MandelbrotSetModel model;
    private MandelbrotSetView view;
    private JFrame mainFrame;

    public MandelbrotSetController(MandelbrotSetModel model1, JFrame mainFrame1) {
        model = model1;
        mainFrame = mainFrame1;
    }

    public void setView(MandelbrotSetView view1){
        view = view1;
    }

    public void removeView(MandelbrotSetView view1){
        if(view == view1)
            view = null;
    }

    @Override
    public void moveCenterToLeft() {
        model.moveCenter(new Point2D.Double(-1, 0), movePixelsX);
    }

    @Override
    public void moveCenterToRight() {
        model.moveCenter(new Point2D.Double(1, 0), movePixelsX);
    }

    @Override
    public void moveCenterUp() {
        model.moveCenter(new Point2D.Double(0, -1), movePixelsY);
    }

    @Override
    public void moveCenterDown() {
        model.moveCenter(new Point2D.Double(0, 1), movePixelsY);
    }
}
