package io.github.mateusz00.MandelbrotSet;

import io.github.mateusz00.MandelbrotSet.Dialogs.ImageGenerateDialog;
import io.github.mateusz00.MandelbrotSet.Dialogs.SettingsDialog;
import io.github.mateusz00.MandelbrotSet.Dialogs.VideoGenerateDialog;
import io.github.mateusz00.MandelbrotSet.RGBPickers.*;
import io.github.mateusz00.MandelbrotSet.Utilities.SwingUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

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
        addSettingsMenu(menuBar);

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
        red.addActionListener((e) -> {
            view.setColoring(new PickerRed());
            view.updateView();
        });
        red.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        colors.add(red);

        JMenuItem darkRed = new JMenuItem("Dark red", KeyEvent.VK_D);
        darkRed.addActionListener((e) -> {
            view.setColoring(new PickerRedDark());
            view.updateView();
        });
        darkRed.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        colors.add(darkRed);

        JMenuItem blue = new JMenuItem("Blue", KeyEvent.VK_B);
        blue.addActionListener((e) -> {
            view.setColoring(new PickerBlue());
            view.updateView();
        });
        blue.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        colors.add(blue);
    }

    private void addSettingsMenu(JMenuBar menuBar) {
        JMenu settings = new JMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_S);

        JMenuItem configure = new JMenuItem("Configure...", KeyEvent.VK_C);
        configure.addActionListener((e) -> {
            SettingsDialog dialog = new SettingsDialog(mainWindow, controller);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setResizable(false);
            dialog.pack();
            SwingUtility.centerComponent(mainWindow.getLocationOnScreen(), mainWindow.getSize(), dialog);
            dialog.setVisible(true);
        });
        configure.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        settings.add(configure);

        settings.addSeparator();
        JMenuItem restoreDefault = new JMenuItem("Restore default settings", KeyEvent.VK_R);
        restoreDefault.addActionListener((e) -> {

        });
        settings.add(restoreDefault);

        JMenuItem importSettings = new JMenuItem("Import settings...", KeyEvent.VK_I);
        importSettings.addActionListener((e) -> {

        });
        settings.add(importSettings);

        JMenuItem exportSettings = new JMenuItem("Export settings...", KeyEvent.VK_E);
        exportSettings.addActionListener((e) -> {

        });
        settings.add(exportSettings);

        menuBar.add(settings);
    }

    void createImageDialog() {
        ImageGenerateDialog dialog = new ImageGenerateDialog(mainWindow, controller);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.pack();
        SwingUtility.centerComponent(mainWindow.getLocationOnScreen(), mainWindow.getSize(), dialog);
        dialog.setVisible(true);
    }

    void createVideoDialog() {
        VideoGenerateDialog dialog = new VideoGenerateDialog(mainWindow, controller);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.pack();
        SwingUtility.centerComponent(mainWindow.getLocationOnScreen(), mainWindow.getSize(), dialog);
        dialog.setVisible(true);
    }
}