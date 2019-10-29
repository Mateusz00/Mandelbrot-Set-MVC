package MandelbrotSet;

import MandelbrotSet.RGBPickers.PickerBlue;
import MandelbrotSet.RGBPickers.PickerRed;
import MandelbrotSet.RGBPickers.PickerRedDark;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class Application
{
    private static final int WIDTH = 850;
    private static final int HEIGHT = 600;
    private JFrame mainWindow = new JFrame("Mandelbrot Set");
    private MandelbrotSetController controller;

    public static void main(String[] args) {
        Application app = new Application();

        SwingUtilities.invokeLater(() -> app.run());
    }

    public void run() {
        MandelbrotSetModel model = new MandelbrotSetModel(new Dimension(WIDTH, HEIGHT));
        controller = new MandelbrotSetController(model);
        MandelbrotSetView view = new MandelbrotSetView(model, controller);
        controller.setView(view);

        addMenuBar(mainWindow, view);
        mainWindow.setResizable(false);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.add(view);
        mainWindow.pack();
        mainWindow.setVisible(true);

        // Generate in different thread so it doesn't block GUI
        new Thread(() -> model.generate()).start();
    }

    private void addMenuBar(JFrame frame, MandelbrotSetView view) {
        JMenuBar menuBar = new JMenuBar();

        addColorsMenu(view, menuBar);

        frame.setJMenuBar(menuBar);
    }

    private void addColorsMenu(MandelbrotSetView view, JMenuBar menuBar) {
        JMenu colors = new JMenu("Colors");
        colors.setMnemonic(KeyEvent.VK_C);
        menuBar.add(colors);

        JMenuItem red = new JMenuItem("Red", KeyEvent.VK_R);
        red.addActionListener((e) -> view.setColoring(new PickerRed()));
        red.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        colors.add(red);

        JMenuItem darkRed = new JMenuItem("Dark red", KeyEvent.VK_D);
        darkRed.addActionListener((e) -> view.setColoring(new PickerRedDark()));
        darkRed.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        colors.add(darkRed);

        JMenuItem blue = new JMenuItem("Blue", KeyEvent.VK_B);
        blue.addActionListener((e) -> view.setColoring(new PickerBlue()));
        blue.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        colors.add(blue);
    }
}
