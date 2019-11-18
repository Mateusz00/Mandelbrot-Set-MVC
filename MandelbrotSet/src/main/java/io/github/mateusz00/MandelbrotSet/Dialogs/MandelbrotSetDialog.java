package io.github.mateusz00.MandelbrotSet.Dialogs;

import io.github.mateusz00.MandelbrotSet.MandelbrotSetController;
import io.github.mateusz00.MandelbrotSet.RGBPickers.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.Point2D;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;

import static io.github.mateusz00.MandelbrotSet.Utilities.SwingUtility.createFieldAndLabel;
import static io.github.mateusz00.MandelbrotSet.Utilities.SwingUtility.getFlowLayoutDefaultPadding;

public class MandelbrotSetDialog extends JDialog
{
    private final static Border formMarginBorder = BorderFactory.createEmptyBorder(0, 0, 2, 0);
    private JFormattedTextField centerX;
    private JFormattedTextField centerY;
    private JFormattedTextField xRange;
    private JFormattedTextField yRange;
    private JFormattedTextField maxIterations;
    private JFormattedTextField escapeRadius;
    private JComboBox colors;
    private JCheckBox smoothColoring;
    private final JPanel mainPanel;
    private MandelbrotSetController controller;

    /**
     * Only adds some panels and components. Does not set default close operation, resizability, visibility etc.
     */
    public MandelbrotSetDialog(JFrame mainWindow, String title, boolean modal, MandelbrotSetController controller) {
        super(mainWindow, title, modal);
        this.controller = controller;

        // Create container
        mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        mainPanel.add(createFormPanel());
        add(mainPanel);
    }

    protected static Border getFormMargin() {
        return formMarginBorder;
    }

    protected static Border getFormPadding() {
        return getFlowLayoutDefaultPadding();
    }

    protected static Border createFormPanelBorder(String title) {
        return BorderFactory.createCompoundBorder(formMarginBorder, createPaddedTitledBorder(title));
    }

    protected static Border createPaddedTitledBorder(String title) {
        return BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title), getFlowLayoutDefaultPadding());
    }

    protected double getCenterXValue() {
        return ((Number) centerX.getValue()).doubleValue();
    }

    protected double getCenterYValue() {
        return ((Number) centerY.getValue()).doubleValue();
    }

    protected double getXRangeValue() {
        return ((Number) xRange.getValue()).doubleValue();
    }

    protected double getYRangeValue() {
        return ((Number) yRange.getValue()).doubleValue();
    }

    protected long getMaxIterationsValue() {
        return ((Number) maxIterations.getValue()).longValue();
    }

    protected long getEscapeRadiusValue() {
        return ((Number) escapeRadius.getValue()).longValue();
    }

    protected RGBPicker getRGBPicker() {
        return ((RGBPicker) colors.getSelectedItem());
    }

    protected boolean isSmoothColoringEnabled() {
        return smoothColoring.isSelected();
    }

    protected void setCenterXValue(double value) {
        centerX.setValue(value);
    }

    protected void setCenterYValue(double value) {
        centerY.setValue(value);
    }

    protected void setXRangeValue(double value) {
        xRange.setValue(value);
    }

    protected void setYRangeValue(double value) {
        yRange.setValue(value);
    }

    protected void setMaxIterationsValue(long value) {
        maxIterations.setValue(value);
    }

    protected void setEscapeRadiusValue(long value) {
        escapeRadius.setValue(value);
    }

    protected void setRGBPicker(RGBPicker rgbPicker) {
        colors.getModel().setSelectedItem(rgbPicker);
    }

    protected void setSmoothColoring(boolean flag) {
        smoothColoring.setSelected(flag);
    }

    /**
     * Main panel has box layout with axis = PAGE_AXIS
     */
    public void addToMainPanel(Component component) {
        mainPanel.add(component);
    }

    /**
     * Sets values of form fields to values currently set in mandelbrot set model
     */
    protected void loadCurrentValues() {
        setCenterXValue(controller.getCenter().getX());
        setCenterYValue(controller.getCenter().getY());
        setXRangeValue(controller.getXRange());
        setYRangeValue(controller.getYRange());
        setMaxIterationsValue(controller.getMaxIterations());
        setEscapeRadiusValue(controller.getEscapeRadius());
        setRGBPicker(controller.getCurrentRGBPicker());
        setSmoothColoring(controller.isSmoothColoringEnabled());
    }

    /**
     * Updates controller with values from form fields
     */
    protected void flushValues() {
        controller.setEscapeRadius(getEscapeRadiusValue());
        controller.setCenter(new Point2D.Double(getCenterXValue(), getCenterYValue()));
        controller.setMaxIterations(getMaxIterationsValue());
        controller.setXRange(getXRangeValue());
        controller.setYRange(getYRangeValue());
        controller.setRGBPicker(getRGBPicker());
        controller.setSmoothColoring(isSmoothColoringEnabled());
    }

    private JPanel createFormPanel() {
        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.PAGE_AXIS));

        // Create formats
        NumberFormat doubleFormat = NumberFormat.getNumberInstance();
        doubleFormat.setMinimumFractionDigits(1);
        doubleFormat.setMaximumFractionDigits(Double.MAX_EXPONENT);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMinimumIntegerDigits(1);
        decimalFormat.setMaximumIntegerDigits(19);
        decimalFormat.setMaximumFractionDigits(0);
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);

        // Add panels to form panel
        formPanel.add(createCenterPanel(doubleFormat));
        formPanel.add(createRangePanel(doubleFormat));
        formPanel.add(createIterationsPanel(decimalFormat));
        formPanel.add(createEscapeRadiusPanel(decimalFormat));
        formPanel.add(createRGBPickerPanel());

        return formPanel;
    }

    private JPanel createCenterPanel(Format doubleFormat) {
        JPanel panelCenter = new JPanel(new GridLayout(1, 2, 5, 5));
        panelCenter.setBorder(createFormPanelBorder("Center"));
        JPanel subPanelCenter1 = new JPanel(new BorderLayout(5, 5));
        JPanel subPanelCenter2 = new JPanel(new BorderLayout(5, 5));

        centerX = createFieldAndLabel(doubleFormat, subPanelCenter1, "x:", BorderLayout.WEST, BorderLayout.CENTER);
        centerY = createFieldAndLabel(doubleFormat, subPanelCenter2, "y:", BorderLayout.WEST, BorderLayout.CENTER);

        panelCenter.add(subPanelCenter1);
        panelCenter.add(subPanelCenter2);

        return panelCenter;
    }

    private JPanel createRangePanel(Format doubleFormat) {
        JPanel panelZoom = new JPanel(new GridLayout(1, 2, 5, 5));
        panelZoom.setBorder(createFormPanelBorder("Range"));
        JPanel subPanelZoom1 = new JPanel(new BorderLayout(5, 5));
        JPanel subPanelZoom2 = new JPanel(new BorderLayout(5, 5));

        xRange = createFieldAndLabel(doubleFormat, subPanelZoom1, "x:", BorderLayout.WEST, BorderLayout.CENTER);
        yRange = createFieldAndLabel(doubleFormat, subPanelZoom2, "y:", BorderLayout.WEST, BorderLayout.CENTER);

        panelZoom.add(subPanelZoom1);
        panelZoom.add(subPanelZoom2);

        return panelZoom;
    }

    private JPanel createIterationsPanel(DecimalFormat decimalFormat) {
        JPanel panelIterations = new JPanel(new BorderLayout(5, 5));
        panelIterations.setBorder(createFormPanelBorder("Max iterations"));

        maxIterations = new JFormattedTextField(decimalFormat);
        maxIterations.setColumns(24);
        maxIterations.setValue(0);
        panelIterations.add(maxIterations);

        return panelIterations;
    }

    private JPanel createEscapeRadiusPanel(DecimalFormat decimalFormat) {
        JPanel panelRadius = new JPanel(new BorderLayout(5, 5));
        panelRadius.setBorder(createFormPanelBorder("Escape radius"));

        escapeRadius = new JFormattedTextField(decimalFormat);
        escapeRadius.setColumns(24);
        escapeRadius.setValue(0);
        panelRadius.add(escapeRadius);

        return panelRadius;
    }

    private JPanel createRGBPickerPanel() {
        JPanel panelRGBPicker = new JPanel();
        panelRGBPicker.setBorder(createFormPanelBorder("Coloring"));
        panelRGBPicker.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        Object[] coloringAlgorithms = {new PickerRed(), new PickerRedDark(), new PickerBlue()};
        colors = new JComboBox(coloringAlgorithms);
        colors.setRenderer(new RGBPickerComboBoxRenderer());
        panelRGBPicker.add(colors);

        smoothColoring = new JCheckBox("Use smooth coloring");
        smoothColoring.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        panelRGBPicker.add(smoothColoring);

        return panelRGBPicker;
    }
}
