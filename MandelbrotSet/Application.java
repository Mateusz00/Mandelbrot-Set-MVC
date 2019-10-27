package MandelbrotSet;

import javax.swing.*;
import java.awt.*;

public class Application
{
    private static final int WIDTH = 850;
    private static final int HEIGHT = 600;
    private static JFrame mainWindow = new JFrame("Mandelbrot Set");
    private static MandelbrotSetController controller;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Application::run);
    }

    private static void run() {
        mainWindow.setResizable(false);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MandelbrotSetModel model = new MandelbrotSetModel(new Dimension(WIDTH, HEIGHT));
        controller = new MandelbrotSetController(model);
        MandelbrotSetView view = new MandelbrotSetView(model, controller);
        controller.setView(view);

        mainWindow.add(view);
        mainWindow.pack();
        mainWindow.setVisible(true);

        Thread t = new Thread(new ModelInitializer(model));
        t.start();
    }

    private static class ModelInitializer implements Runnable
    {
        private MandelbrotSetModel model;

        ModelInitializer(MandelbrotSetModel m) {
            model = m;
        }

        @Override
        public void run() {
            model.generate();
        }
    }
}
