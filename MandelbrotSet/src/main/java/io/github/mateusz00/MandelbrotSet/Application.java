package io.github.mateusz00.MandelbrotSet;

import io.github.mateusz00.MandelbrotSet.RGBPickers.*;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;

public class Application
{
    private static final int WIDTH = 850;
    private static final int HEIGHT = 600;
    private final JFrame mainWindow = new JFrame("Mandelbrot Set");
    private final JFileChooser imageFileChooser;
    private final JFileChooser videoFileChooser;
    private MandelbrotSetController controller;

    public Application() {
        // Set up imageFileChooser
        ExtensionFilter PNGExtension = new ExtensionFilter("PNG (*.png)", "png");
        ExtensionFilter JPGExtension = new ExtensionFilter("JPG (*.jpg)", "jpg");
        PNGExtension.setEnforcedSaveExtension("png");
        JPGExtension.setEnforcedSaveExtension("jpg");

        imageFileChooser = new JFileChooser();
        imageFileChooser.setAcceptAllFileFilterUsed(false);
        imageFileChooser.addChoosableFileFilter(PNGExtension);
        imageFileChooser.addChoosableFileFilter(JPGExtension);

        // Set up videoFileChooser
        ExtensionFilter WEBMExtension = new ExtensionFilter("WEBM (*.webm)", "webm");
        WEBMExtension.setEnforcedSaveExtension("webm");

        videoFileChooser = new JFileChooser();
        videoFileChooser.setAcceptAllFileFilterUsed(false);
        videoFileChooser.addChoosableFileFilter(WEBMExtension);
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

    void createImageDialog() {
        // Constants
        final String CENTER = BorderLayout.CENTER;
        final String WEST = BorderLayout.WEST;

        // Set up dialog
        JDialog dialog = new JDialog(mainWindow, "Generate image", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        // Create container
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BorderLayout());

        // Add panels to main panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel lastPanel = new JPanel();
        lastPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        mainPanel.add(lastPanel, BorderLayout.PAGE_END);

        // Add panels to form panel
        Border marginBorder = BorderFactory.createEmptyBorder(0, 0, 2, 0);
        Border paddingBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);

        JPanel panelCenter = new JPanel(new GridLayout(1, 2, 5, 5));
        panelCenter.setBorder(BorderFactory.createCompoundBorder(marginBorder, BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Center"), paddingBorder)));
        JPanel panelZoom = new JPanel(new GridLayout(1, 2, 5, 5));
        panelZoom.setBorder(BorderFactory.createCompoundBorder(marginBorder, BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Zoom"), paddingBorder)));
        JPanel panelIterations = new JPanel(new BorderLayout(5, 5));
        panelIterations.setBorder(BorderFactory.createCompoundBorder(marginBorder, BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Max iterations"), paddingBorder)));
        JPanel panelRadius = new JPanel(new BorderLayout(5, 5));
        panelRadius.setBorder(BorderFactory.createCompoundBorder(marginBorder, BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Escape radius"), paddingBorder)));
        JPanel panelRGBPicker = new JPanel();
        panelRGBPicker.setBorder(BorderFactory.createCompoundBorder(marginBorder, BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Coloring"), paddingBorder)));
        panelRGBPicker.setLayout(new BorderLayout());
        JPanel panelFileChooser = new JPanel(new BorderLayout(5, 5));
        panelFileChooser.setBorder(BorderFactory.createCompoundBorder(marginBorder, BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Choose destination"), paddingBorder)));
        formPanel.add(panelCenter);
        formPanel.add(panelZoom);
        formPanel.add(panelIterations);
        formPanel.add(panelRadius);
        formPanel.add(panelRGBPicker);
        formPanel.add(panelFileChooser);

        // Create formats
        NumberFormat doubleFormat = NumberFormat.getNumberInstance();
        doubleFormat.setMinimumFractionDigits(1);
        doubleFormat.setMaximumFractionDigits(Double.MAX_EXPONENT);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumIntegerDigits(19);
        decimalFormat.setMaximumFractionDigits(0);
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);

        // Center panel
        JPanel subPanelCenter1 = new JPanel(new BorderLayout(5, 5));
        JPanel subPanelCenter2 = new JPanel(new BorderLayout(5, 5));
        final JFormattedTextField centerX = createFieldAndLabel(doubleFormat, subPanelCenter1, "x:", WEST, CENTER);
        final JFormattedTextField centerY = createFieldAndLabel(doubleFormat, subPanelCenter2, "y:", WEST, CENTER);
        panelCenter.add(subPanelCenter1);
        panelCenter.add(subPanelCenter2);

        // Zoom panel
        JPanel subPanelZoom1 = new JPanel(new BorderLayout(5, 5));
        JPanel subPanelZoom2 = new JPanel(new BorderLayout(5, 5));
        final JFormattedTextField zoomX = createFieldAndLabel(doubleFormat, subPanelZoom1, "x:", WEST, CENTER);
        final JFormattedTextField zoomY = createFieldAndLabel(doubleFormat, subPanelZoom2, "y:", WEST, CENTER);
        panelZoom.add(subPanelZoom1);
        panelZoom.add(subPanelZoom2);

        final JFormattedTextField maxIterations = new JFormattedTextField(decimalFormat);
        maxIterations.setColumns(24);
        panelIterations.add(maxIterations);

        final JFormattedTextField escapeRadius = new JFormattedTextField(decimalFormat);
        escapeRadius.setColumns(24);
        panelRadius.add(escapeRadius);

        // Choose color
        Object[] coloringAlgorithms = {new PickerRed(), new PickerRedDark(), new PickerBlue()};
        final JComboBox colors = new JComboBox(coloringAlgorithms);
        colors.setRenderer(new RGBPickerComboBoxRenderer());
        panelRGBPicker.add(colors, BorderLayout.WEST);

        // File chooser
        final JTextField saveDestination = new JTextField("", 16);
        saveDestination.setEditable(false);
        panelFileChooser.add(saveDestination, BorderLayout.CENTER);

        JButton fileChooseButton = new JButton("Save as...");
        fileChooseButton.addActionListener((e) -> {
            int returnVal = imageFileChooser.showSaveDialog(fileChooseButton);

            if(returnVal == JFileChooser.APPROVE_OPTION) {
                // Obtain filename and enforce chosen extension
                String fileName = imageFileChooser.getSelectedFile().getName();
                fileName = ((ExtensionFilter) imageFileChooser.getFileFilter()).enforceExtension(fileName);

                // Construct path and update saveDestination
                File file = new File(imageFileChooser.getSelectedFile().getParent(), fileName);
                saveDestination.setText(file.getAbsolutePath());
            }
        });
        panelFileChooser.add(fileChooseButton, BorderLayout.EAST);

        // Last panel components
        JButton currentDataGetter = new JButton("Current data");
        currentDataGetter.addActionListener((e) -> {
            centerX.setValue(controller.getCenter().getX());
            centerY.setValue(controller.getCenter().getY());
            zoomX.setValue((controller.getZoom())[0]);
            zoomY.setValue((controller.getZoom())[1]);
            maxIterations.setValue(controller.getMaxIterations());
            escapeRadius.setValue(controller.getEscapeRadius());
            colors.getModel().setSelectedItem(controller.getCurrentRGBPicker());
        });
        lastPanel.add(currentDataGetter);

        JButton generateButton = new JButton("Generate");
        generateButton.addActionListener((e) -> {
            // Save file destination have to be chosen
            if(!saveDestination.getText().equals("")) {
                // Generate mandelbrot set
                double centerXVal = ((Number) centerX.getValue()).doubleValue();
                double centerYVal = ((Number) centerY.getValue()).doubleValue();
                long escapeRadiusVal = ((Number) escapeRadius.getValue()).longValue();
                long maxIterationsVal = ((Number) maxIterations.getValue()).longValue();
                double zoomXVal = ((Number) zoomX.getValue()).doubleValue();
                double zoomYVal = ((Number) zoomY.getValue()).doubleValue();

                controller.setEscapeRadius(escapeRadiusVal);
                controller.setCenter(new Point2D.Double(centerXVal, centerYVal));
                controller.setMaxIterations(maxIterationsVal);
                controller.setZoom(new double[]{zoomXVal, zoomYVal});
                controller.setRGBPicker((RGBPicker) colors.getSelectedItem());
                controller.generateNewSet();
                BufferedImage img = controller.getBufferedImage();

                // Write generated mandelbrot set to file
                File file = new File(saveDestination.getText());

                try {
                    String extension = ((ExtensionFilter) imageFileChooser.getFileFilter()).getEnforcedSaveExtension();
                    ImageIO.write(img, extension, file);
                }
                catch(IOException exception) {
                    exception.printStackTrace();
                }
            }
            else
                JOptionPane.showMessageDialog(dialog, "Error: Choose save file destination!",
                        "Error", JOptionPane.ERROR_MESSAGE);
        });
        lastPanel.add(generateButton);

        dialog.add(mainPanel);
        dialog.pack();

        // Position dialog (Dialog's center should be at the same position as mainFrame's center)
        Point center = getMainFrameCenterPosition();
        dialog.setLocation(center.x - dialog.getWidth() / 2, center.y - dialog.getHeight() / 2);
        dialog.setVisible(true);
    }

    private Point getMainFrameCenterPosition() {
        Dimension dimension = mainWindow.getSize();
        Point centerTopLeftDistance = new Point(dimension.width / 2, dimension.height / 2);
        Point topLeft = mainWindow.getLocationOnScreen();

        return new Point(centerTopLeftDistance.x + topLeft.x, centerTopLeftDistance.y + topLeft.y);
    }

    private class ExtensionFilter extends FileFilter
    {
        private ArrayList<String> allowedExtensions;
        private String description;
        private String enforcedSaveExtension;

        public ExtensionFilter(String description, String... allowExtensions) {
            this.description = description;
            allowedExtensions = new ArrayList<>(allowExtensions.length);

            for(String extension : allowExtensions)
                allowedExtensions.add(extension);
        }

        public String getEnforcedSaveExtension() {
            return enforcedSaveExtension;
        }

        public void setEnforcedSaveExtension(String enforcedSaveExtension) {
            this.enforcedSaveExtension = enforcedSaveExtension;
        }

        public String enforceExtension(String fileName) {
            // Get rid of invalid extensions and/or add correct one
            fileName = Utility.removeExtension(fileName) + "." + enforcedSaveExtension;

            return fileName;
        }

        @Override
        public boolean accept(File f) {
            if(f.isDirectory())
                return true;

            // Get extension
            String fileName = f.getName(), extension;
            int extensionIndex = fileName.lastIndexOf('.');

            if(extensionIndex != -1) {
                extension = fileName.substring(extensionIndex + 1).toLowerCase();

                for(String allowedExtension : allowedExtensions) {
                    if(extension.equals(allowedExtension))
                        return true;
                }
            }

            return false;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    private class RGBPickerComboBoxRenderer extends BasicComboBoxRenderer
    {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setText(((RGBPicker) value).getDescription());

            return this;
        }
    }

    /**
     * Creates FormattedTextField and label and adds them to specified panel
     * @return Created JFormattedTextField
     */
    private JFormattedTextField createFieldAndLabel(Format format, JPanel panel, String label) {
        return createFieldAndLabel(format, panel, label, null, null);
    }

    /**
     * Creates FormattedTextField and label and adds them to specified panel
     * @return Created JFormattedTextField
     */
    private JFormattedTextField createFieldAndLabel(Format format, JPanel panel, String label, Object labelConstraints,
                                                    Object fieldConstraints) {
        JFormattedTextField field = new JFormattedTextField(format);
        field.setColumns(10);
        field.setValue(0);

        JLabel centerXLabel = new JLabel(label);
        centerXLabel.setLabelFor(field);
        panel.add(centerXLabel, labelConstraints);
        panel.add(field, fieldConstraints);

        return field;
    }

    void createVideoDialog() {
        // Constants
        final String CENTER = BorderLayout.CENTER;
        final String WEST = BorderLayout.WEST;

        // Set up dialog
        JDialog dialog = new JDialog(mainWindow, "Generate video", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        // Create container
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BorderLayout());

        // Add panels to main panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel lastPanel = new JPanel();
        lastPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        mainPanel.add(lastPanel, BorderLayout.PAGE_END);

        // Add panels to form panel
        Border marginBorder = BorderFactory.createEmptyBorder(0, 0, 2, 0);
        Border paddingBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);

        JPanel panelCenter = new JPanel(new GridLayout(1, 2, 5, 5));
        panelCenter.setBorder(BorderFactory.createCompoundBorder(marginBorder, BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Center"), paddingBorder)));
        JPanel panelZoom = new JPanel(new GridLayout(1, 2, 5, 5));
        panelZoom.setBorder(BorderFactory.createCompoundBorder(marginBorder, BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Zoom"), paddingBorder)));
        JPanel panelIterations = new JPanel(new BorderLayout(5, 5));
        panelIterations.setBorder(BorderFactory.createCompoundBorder(marginBorder, BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Max iterations"), paddingBorder)));
        JPanel panelRadius = new JPanel(new BorderLayout(5, 5));
        panelRadius.setBorder(BorderFactory.createCompoundBorder(marginBorder, BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Escape radius"), paddingBorder)));
        JPanel panelRGBPicker = new JPanel();
        panelRGBPicker.setBorder(BorderFactory.createCompoundBorder(marginBorder, BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Coloring"), paddingBorder)));
        panelRGBPicker.setLayout(new BorderLayout());
        JPanel panelFileChooser = new JPanel(new BorderLayout(5, 5));
        panelFileChooser.setBorder(BorderFactory.createCompoundBorder(marginBorder, BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Choose destination"), paddingBorder)));
        JPanel panelVideoSettings = new JPanel();
        panelVideoSettings.setBorder(BorderFactory.createCompoundBorder(marginBorder, BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Video settings"), paddingBorder)));
        panelVideoSettings.setLayout(new BoxLayout(panelVideoSettings, BoxLayout.PAGE_AXIS));
        formPanel.add(panelCenter);
        formPanel.add(panelZoom);
        formPanel.add(panelIterations);
        formPanel.add(panelRadius);
        formPanel.add(panelRGBPicker);
        formPanel.add(panelFileChooser);
        formPanel.add(panelVideoSettings);

        // Create formats
        NumberFormat doubleFormat = NumberFormat.getNumberInstance();
        doubleFormat.setMinimumFractionDigits(1);
        doubleFormat.setMaximumFractionDigits(Double.MAX_EXPONENT);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumIntegerDigits(19);
        decimalFormat.setMaximumFractionDigits(0);
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);

        // Center panel
        JPanel subPanelCenter1 = new JPanel(new BorderLayout(5, 5));
        JPanel subPanelCenter2 = new JPanel(new BorderLayout(5, 5));
        final JFormattedTextField centerX = createFieldAndLabel(doubleFormat, subPanelCenter1, "x:", WEST, CENTER);
        final JFormattedTextField centerY = createFieldAndLabel(doubleFormat, subPanelCenter2, "y:", WEST, CENTER);
        panelCenter.add(subPanelCenter1);
        panelCenter.add(subPanelCenter2);

        // Zoom panel
        JPanel subPanelZoom1 = new JPanel(new BorderLayout(5, 5));
        JPanel subPanelZoom2 = new JPanel(new BorderLayout(5, 5));
        final JFormattedTextField zoomX = createFieldAndLabel(doubleFormat, subPanelZoom1, "x:", WEST, CENTER);
        final JFormattedTextField zoomY = createFieldAndLabel(doubleFormat, subPanelZoom2, "y:", WEST, CENTER);
        panelZoom.add(subPanelZoom1);
        panelZoom.add(subPanelZoom2);

        final JFormattedTextField maxIterations = new JFormattedTextField(decimalFormat);
        maxIterations.setColumns(24);
        panelIterations.add(maxIterations);

        final JFormattedTextField escapeRadius = new JFormattedTextField(decimalFormat);
        escapeRadius.setColumns(24);
        panelRadius.add(escapeRadius);

        // Choose color
        Object[] coloringAlgorithms = {new PickerRed(), new PickerRedDark(), new PickerBlue()};
        final JComboBox colors = new JComboBox(coloringAlgorithms);
        colors.setRenderer(new RGBPickerComboBoxRenderer());
        panelRGBPicker.add(colors, BorderLayout.WEST);

        // File chooser
        final JTextField saveDestination = new JTextField("");
        saveDestination.setEditable(false);
        panelFileChooser.add(saveDestination, BorderLayout.CENTER);

        JButton fileChooseButton = new JButton("Save as...");
        fileChooseButton.addActionListener((e) -> {
            int returnVal = videoFileChooser.showSaveDialog(fileChooseButton);

            if(returnVal == JFileChooser.APPROVE_OPTION) {
                // Obtain filename and enforce chosen extension
                String fileName = videoFileChooser.getSelectedFile().getName();
                fileName = ((ExtensionFilter) videoFileChooser.getFileFilter()).enforceExtension(fileName);

                // Construct path and update saveDestination
                File file = new File(videoFileChooser.getSelectedFile().getParent(), fileName);
                saveDestination.setText(file.getAbsolutePath());
            }
        });
        panelFileChooser.add(fileChooseButton, BorderLayout.EAST);

        // Video settings panel
        JPanel videoSubPanel1 = new JPanel(new GridLayout(0, 2, 15, 2));

        final JFormattedTextField frames = createFieldAndLabel(decimalFormat, videoSubPanel1, "Frames:");
        final JFormattedTextField zoomPercent = createFieldAndLabel(doubleFormat, videoSubPanel1, "Zoom(%):");
        zoomPercent.setToolTipText("Sets how much % will it zoom in/out with every frame (Negative for zooming out)");
        final JFormattedTextField maxIterationsMultiplier = createFieldAndLabel(doubleFormat, videoSubPanel1,
                "Max iterations multiplier:");
        maxIterationsMultiplier.setToolTipText("Affects both coloring and computation speed (Higher = slower)");

        panelVideoSettings.add(videoSubPanel1);
        panelVideoSettings.add(videoSubPanel1);

        JPanel videoSubPanel3 = new JPanel();
        final JCheckBox keepImages = new JCheckBox("Keep images");
        keepImages.setToolTipText("Keep generated images(every frame is saved as an image first before creating video)");

        final JCheckBox generateVideo = new JCheckBox("Generate video");
        generateVideo.setToolTipText("Uncheck it if you want to create video from images with settings and " +
                "codec different than default");
        generateVideo.addItemListener(((e) -> {
            if(((JCheckBox) e.getSource()).isSelected())
                keepImages.setEnabled(true);
            else {
                keepImages.setEnabled(false);
                keepImages.setSelected(true);
            }
        }));
        generateVideo.setSelected(true);

        videoSubPanel3.add(generateVideo);
        videoSubPanel3.add(keepImages);
        panelVideoSettings.add(videoSubPanel3);

        // Last panel components
        JButton currentDataGetter = new JButton("Current data");
        currentDataGetter.addActionListener((e) -> {
            centerX.setValue(controller.getCenter().getX());
            centerY.setValue(controller.getCenter().getY());
            zoomX.setValue((controller.getZoom())[0]);
            zoomY.setValue((controller.getZoom())[1]);
            maxIterations.setValue(controller.getMaxIterations());
            escapeRadius.setValue(controller.getEscapeRadius());
            colors.getModel().setSelectedItem(controller.getCurrentRGBPicker());
            zoomPercent.setValue(controller.getZoomPercent());
            maxIterationsMultiplier.setValue(controller.getMaxIterationsMultiplier());
        });
        lastPanel.add(currentDataGetter);

        JButton generateButton = new JButton("Generate");
        generateButton.addActionListener((e) -> {
            // Save file destination have to be chosen
            if(!saveDestination.getText().equals("")) {
                // Get data from components and update model, view and controller
                long framesVal = ((Number) frames.getValue()).longValue();
                if(framesVal <= 0)
                    return;

                double centerXVal = ((Number) centerX.getValue()).doubleValue();
                double centerYVal = ((Number) centerY.getValue()).doubleValue();
                long escapeRadiusVal = ((Number) escapeRadius.getValue()).longValue();
                long maxIterationsVal = ((Number) maxIterations.getValue()).longValue();
                double zoomXVal = ((Number) zoomX.getValue()).doubleValue();
                double zoomYVal = ((Number) zoomY.getValue()).doubleValue();
                double zoomPercentVal = ((Number) zoomPercent.getValue()).doubleValue();
                double maxIterationsMultiplierVal = ((Number) maxIterationsMultiplier.getValue()).doubleValue();
                int digits = Utility.digitsNumber(framesVal);

                double zoomPercentOld = controller.getZoomPercent();
                double maxIterationsMultiplierOld = controller.getMaxIterationsMultiplier();

                controller.setEscapeRadius(escapeRadiusVal);
                controller.setCenter(new Point2D.Double(centerXVal, centerYVal));
                controller.setMaxIterations(maxIterationsVal);
                controller.setZoom(new double[]{zoomXVal, zoomYVal});
                controller.setRGBPicker((RGBPicker) colors.getSelectedItem());
                controller.setZoomPercent(zoomPercentVal);
                controller.setMaxIterationsMultiplier(maxIterationsMultiplierVal);

                // First create directory and generate all frames and save them as individual images
                File destination = new File(saveDestination.getText());
                File framesDir = new File(Utility.removeExtension(destination.getAbsolutePath()) + "_frames");
                framesDir.mkdirs();

                for(long i = 0; i < framesVal; ++i) {
                    // Generate mandelbrot set
                    controller.generateNewSet();
                    BufferedImage img = controller.getBufferedImage();

                    // Write generated mandelbrot set to file
                    String fileNumber = String.format("%0" + digits + "d", i);
                    File file = new File(framesDir + "/frame" + fileNumber + ".png");

                    try {
                        ImageIO.write(img, "png", file);
                    }
                    catch(IOException exception) {
                        exception.printStackTrace();
                    }

                    controller.zoomInNoMultithreading();
                }

                // Use ffmpeg (if there is one and user has selected generate video)
                if(generateVideo.isSelected()) {
                    try {
                        FFmpeg ffmpeg = new FFmpeg("ffmpeg/ffmpeg");
                        FFprobe ffprobe = new FFprobe("ffmpeg/ffprobe");

                        FFmpegBuilder builder = new FFmpegBuilder()
                                .setInput(framesDir + "/frame%0" + digits + "d.png")
                                .overrideOutputFiles(true)
                                .addOutput(saveDestination.getText())
                                    .setFormat("webm")
                                    .setVideoCodec("libvpx")
                                    .setVideoFrameRate(FFmpeg.FPS_24)
                                    .addExtraArgs("-fpre", "ffmpeg/libvpx.ffpreset")
                                    .addExtraArgs("-quality", "best")
                                    .done();

                        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
                        executor.createJob(builder).run();
                    }
                    catch(IOException ee) {
                        ee.printStackTrace();
                        JOptionPane.showMessageDialog(dialog, "Error: Couldn't find ffmpeg/ffprobe" +
                                        " in ffmpeg directory", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

                // Delete images if user didn't check keep images checkbox
                if(!keepImages.isSelected()) {
                    try {
                        FileUtils.deleteDirectory(framesDir);
                    }
                    catch(IOException ee) {
                        ee.printStackTrace();
                    }
                }

                // Set old values that were overwritten by video settings
                controller.setZoomPercent(zoomPercentOld);
                controller.setMaxIterationsMultiplier(maxIterationsMultiplierOld);
            }
            else
                JOptionPane.showMessageDialog(dialog, "Error: Choose save file destination!",
                        "Error", JOptionPane.ERROR_MESSAGE);
        });
        lastPanel.add(generateButton);

        dialog.add(mainPanel);
        dialog.pack();

        // Position dialog (Dialog's center should be at the same position as mainFrame's center)
        Point center = getMainFrameCenterPosition();
        dialog.setLocation(center.x - dialog.getWidth() / 2, center.y - dialog.getHeight() / 2);
        dialog.setVisible(true);
    }
}