package io.github.mateusz00.MandelbrotSet;

import io.github.mateusz00.MandelbrotSet.Dialogs.ExtensionFilter;
import io.github.mateusz00.MandelbrotSet.Dialogs.ImageGenerateDialog;
import io.github.mateusz00.MandelbrotSet.Dialogs.SettingsDialog;
import io.github.mateusz00.MandelbrotSet.Dialogs.VideoGenerateDialog;
import io.github.mateusz00.MandelbrotSet.RGBPickers.*;
import io.github.mateusz00.MandelbrotSet.Utilities.SwingUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class Application
{
    private static final int WIDTH = 850;
    private static final int HEIGHT = 600;
    private final JFrame mainWindow = new JFrame("Mandelbrot Set");
    private MandelbrotSetController controller;
    private final JFileChooser settingsChooser;
    private final JPanel bindingsPanel;
    private final GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
    private final JMenuBar menuBar = new JMenuBar();

    public Application() {
        // Set up settingsChooser
        ExtensionFilter ConfigurationExtension = new ExtensionFilter("Configuration File", "cfg");
        ConfigurationExtension.setEnforcedSaveExtension("cfg");

        settingsChooser = new JFileChooser();
        settingsChooser.setAcceptAllFileFilterUsed(false);
        settingsChooser.addChoosableFileFilter(ConfigurationExtension);

        mainWindow.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                controller.setMandelbrotSize(((JFrame) e.getComponent()).getContentPane().getSize());
            }
        });

        // Initialize bindingsPanel
        bindingsPanel = new JPanel(new GridLayout(5, 2, 0, 10));
        bindingsPanel.add(new JLabel("Z:"));
        bindingsPanel.add(new JLabel("Zoom in"));
        bindingsPanel.add(new JLabel("X:"));
        bindingsPanel.add(new JLabel("Zoom out"));
        bindingsPanel.add(new JLabel("Arrows:"));
        bindingsPanel.add(new JLabel("Move center"));
        bindingsPanel.add(new JLabel("LMB:"));
        bindingsPanel.add(new JLabel("Move center"));
        bindingsPanel.add(new JLabel("RMB:"));
        bindingsPanel.add(new JLabel("Zoom in by selecting"));
    }

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
        mainWindow.setResizable(true);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.add(view);
        new MandelbrotSetInitializer().execute();
        mainWindow.pack();
        mainWindow.setVisible(true);
    }

    private void addMenuBar(JFrame frame, MandelbrotSetView view) {
        addColorsMenu(view, menuBar);
        addGenerateMenu(menuBar);
        addSettingsMenu(menuBar);
        addHelpMenu(menuBar);

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
            controller.restoreDefaultSettings();
            new Thread(() -> controller.generateNewSet()).start();
        });
        settings.add(restoreDefault);

        JMenuItem importSettings = new JMenuItem("Import settings...", KeyEvent.VK_I);
        importSettings.addActionListener((e) -> {
            int returnVal = settingsChooser.showSaveDialog(mainWindow);

            if(returnVal == JFileChooser.APPROVE_OPTION) {
                controller.importSettings(settingsChooser.getSelectedFile());
                new Thread(() -> controller.generateNewSet()).start();
            }
        });
        settings.add(importSettings);

        JMenuItem exportSettings = new JMenuItem("Export settings...", KeyEvent.VK_E);
        exportSettings.addActionListener((e) -> {
            int returnVal = settingsChooser.showSaveDialog(mainWindow);

            if(returnVal == JFileChooser.APPROVE_OPTION) {
                // Obtain filename and enforce chosen extension
                String fileName = settingsChooser.getSelectedFile().getName();
                fileName = ((ExtensionFilter) settingsChooser.getFileFilter()).enforceExtension(fileName);

                // Construct path
                File file = new File(settingsChooser.getSelectedFile().getParent(), fileName);
                controller.exportSettings(file);
            }
        });
        settings.add(exportSettings);

        settings.addSeparator();
        JCheckBoxMenuItem fullscreen = new JCheckBoxMenuItem("Fullscreen");
        fullscreen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        fullscreen.addItemListener((e) -> {
            if(((JCheckBoxMenuItem) e.getSource()).isSelected()) {
                mainWindow.dispose();
                mainWindow.setUndecorated(true);
                device.setFullScreenWindow(mainWindow);
                mainWindow.setVisible(true);
            }
            else {
                mainWindow.dispose();
                mainWindow.setUndecorated(false);
                device.setFullScreenWindow(null);
                mainWindow.setVisible(true);
            }
        });
        settings.add(fullscreen);

        menuBar.add(settings);
    }

    private void addHelpMenu(JMenuBar menuBar) {
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem binds = new JMenuItem("Key bindings");
        binds.setMnemonic(KeyEvent.VK_K);
        binds.addActionListener((e) ->
            JOptionPane.showMessageDialog(mainWindow, bindingsPanel, "Bindings", JOptionPane.PLAIN_MESSAGE));
        helpMenu.add(binds);

        JMenuItem about = new JMenuItem("About");
        about.setMnemonic(KeyEvent.VK_A);
        about.addActionListener((e) ->
            JOptionPane.showMessageDialog(mainWindow, "Author: Mateusz Sęk\nSource code: " +
                            "https://github.com/Mateusz00/Mandelbrot-Set-MVC", "About", JOptionPane.PLAIN_MESSAGE));
        helpMenu.add(about);

        menuBar.add(helpMenu);
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

    private class MandelbrotSetInitializer extends SwingWorker<Void, Void>
    {
        @Override
        protected Void doInBackground() {
            controller.generateNewSet();

            return null;
        }

        @Override
        protected void done() {
            mainWindow.pack();
            mainWindow.setVisible(true);
        }
    }
}