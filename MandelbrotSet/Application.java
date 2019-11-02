package MandelbrotSet;

import MandelbrotSet.RGBPickers.PickerBlue;
import MandelbrotSet.RGBPickers.PickerRed;
import MandelbrotSet.RGBPickers.PickerRedDark;
import MandelbrotSet.RGBPickers.RGBPicker;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
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

        JPanel panelCenter = new JPanel();
        panelCenter.setBorder(BorderFactory.createCompoundBorder(marginBorder,
                BorderFactory.createTitledBorder("Center")));
        JPanel panelZoom = new JPanel();
        panelZoom.setBorder(BorderFactory.createCompoundBorder(marginBorder,
                BorderFactory.createTitledBorder("Zoom")));
        JPanel panelIterations = new JPanel();
        panelIterations.setBorder(BorderFactory.createCompoundBorder(marginBorder,
                BorderFactory.createTitledBorder("Max iterations")));
        JPanel panelRadius = new JPanel();
        panelRadius.setBorder(BorderFactory.createCompoundBorder(marginBorder,
                BorderFactory.createTitledBorder("Escape radius")));
        JPanel panelRGBPicker = new JPanel();
        panelRGBPicker.setBorder(BorderFactory.createCompoundBorder(marginBorder,
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Coloring"),
                        BorderFactory.createEmptyBorder(3, 3, 4, 3))));
        panelRGBPicker.setLayout(new BorderLayout());
        JPanel panelFileChooser = new JPanel();
        panelFileChooser.setBorder(BorderFactory.createCompoundBorder(marginBorder,
                BorderFactory.createTitledBorder("Choose destination")));
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

        // Add formatted text fields
        JFormattedTextField centerX = createFieldAndLabel(doubleFormat, panelCenter, "x:");
        JFormattedTextField centerY = createFieldAndLabel(doubleFormat, panelCenter, "y:");
        JFormattedTextField zoomX = createFieldAndLabel(doubleFormat, panelZoom, "x:");
        JFormattedTextField zoomY = createFieldAndLabel(doubleFormat, panelZoom, "y:");

        JFormattedTextField maxIterations = new JFormattedTextField(decimalFormat);
        maxIterations.setColumns(24);
        panelIterations.add(maxIterations);

        JFormattedTextField escapeRadius = new JFormattedTextField(decimalFormat);
        escapeRadius.setColumns(24);
        panelRadius.add(escapeRadius);

        // Choose color
        Object[] coloringAlgorithms = {new PickerRed(), new PickerRedDark(), new PickerBlue()};
        JComboBox colors = new JComboBox(coloringAlgorithms);
        colors.setRenderer(new RGBPickerComboBoxRenderer());
        panelRGBPicker.add(colors, BorderLayout.WEST);

        // File chooser
        final JTextField saveDestination = new JTextField("", 16);
        saveDestination.setEditable(false);
        panelFileChooser.add(saveDestination);

        JButton fileChooseButton = new JButton("Save as...");
        fileChooseButton.addActionListener((e) -> {
            // TODO
        });
        panelFileChooser.add(fileChooseButton);

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
            // TODO
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
     */
    private JFormattedTextField createFieldAndLabel(Format format, JPanel panel, String label) {
        JFormattedTextField field = new JFormattedTextField(format);
        field.setColumns(10);
        field.setValue(0);

        JLabel centerXLabel = new JLabel(label);
        centerXLabel.setLabelFor(field);
        panel.add(centerXLabel);
        panel.add(field);

        return field;
    }

    void createVideoDialog () {
        JDialog dialog = new JDialog(mainWindow, "Generate video", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // TODO

        dialog.pack();
        dialog.setVisible(true);
    }
}
