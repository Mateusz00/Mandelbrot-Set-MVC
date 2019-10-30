package MandelbrotSet;

import MandelbrotSet.RGBPickers.PickerBlue;
import MandelbrotSet.RGBPickers.PickerRed;
import MandelbrotSet.RGBPickers.PickerRedDark;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;

public class Application
{
    private static final int WIDTH = 850;
    private static final int HEIGHT = 600;
    private final JFrame mainWindow = new JFrame("Mandelbrot Set");
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
        mainWindow.getContentPane().setPreferredSize(new Dimension(WIDTH, HEIGHT));
        mainWindow.pack();
        mainWindow.setVisible(true);

        // Generate in different thread so it doesn't block GUI
        new Thread(() -> model.generate()).start();
    }

    private void addMenuBar(JFrame frame, MandelbrotSetView view) {
        JMenuBar menuBar = new JMenuBar();

        addColorsMenu(view, menuBar);
        addGenerateMenu(menuBar);

        frame.setJMenuBar(menuBar);
    }

    private void addGenerateMenu(JMenuBar menuBar) {
        JMenu generateMenu = new JMenu("Generate");
        generateMenu.setMnemonic(KeyEvent.VK_G);
        menuBar.add(generateMenu);

        JMenuItem toImage = new JMenuItem("Image...");
        toImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.SHIFT_MASK));
        toImage.setMnemonic(KeyEvent.VK_I);
        toImage.addActionListener((e) -> createImageDialog());
        generateMenu.add(toImage);

        JMenuItem toVideo = new JMenuItem("Video...");
        toVideo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.SHIFT_MASK));
        toVideo.setMnemonic(KeyEvent.VK_V);
        toVideo.addActionListener((e) -> createVideoDialog());
        generateMenu.add(toVideo);
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

    void createImageDialog() {
        JDialog dialog = new JDialog(mainWindow, "Generate image", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.add(mainPanel);

        NumberFormat doubleFormatter = NumberFormat.getNumberInstance();
        doubleFormatter.setMinimumFractionDigits(1);
        doubleFormatter.setMaximumFractionDigits(Double.MAX_EXPONENT);

        JFormattedTextField centerX = new JFormattedTextField(doubleFormatter);
        centerX.setColumns(10);
        centerX.setValue(0);
        mainPanel.add(centerX);

        // TODO

        JFormattedTextField centerY = new JFormattedTextField(doubleFormatter);
        centerY.setValue(0);
        centerY.setColumns(10);
        mainPanel.add(centerY);

        dialog.pack();
        dialog.setVisible(true);
    }

    void createVideoDialog () {
        JDialog dialog = new JDialog(mainWindow, "Generate video", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // TODO

        dialog.pack();
        dialog.setVisible(true);
    }
}

/**
 * add setModel to view. Remove model from ctor of view. Add listeners in setModel
 * Controller sets view's model
 * Controller invokes view's onRemoval that deletes listeners from model's list
 *
 * **/