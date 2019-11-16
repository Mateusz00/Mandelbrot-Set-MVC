package io.github.mateusz00.MandelbrotSet.Dialogs;

import io.github.mateusz00.MandelbrotSet.MandelbrotSetController;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

import static io.github.mateusz00.MandelbrotSet.Utilities.SwingUtility.createFieldAndLabel;

public class SettingsDialog extends MandelbrotSetDialog
{
    private MandelbrotSetController controller;
    private JFormattedTextField zoomPercent;
    private JFormattedTextField maxIterationsMultiplier;
    private JButton saveButton;
    /**
     * Only adds some panels and components. Does not set default close operation, resizability, visibility etc.
     */
    public SettingsDialog(JFrame mainWindow, MandelbrotSetController controller) {
        super(mainWindow, "Settings", true, controller);
        this.controller = controller;

        addToMainPanel(createZoomingPanel());
        addToMainPanel(createSavePanel());
        loadCurrentValues();
    }

    @Override
    protected void loadCurrentValues() {
        super.loadCurrentValues();
        zoomPercent.setValue(controller.getZoomStep());
        maxIterationsMultiplier.setValue(controller.getMaxIterationsMultiplier());
    }

    @Override
    protected void flushValues() {
        super.flushValues();
        controller.setZoomStep(((Number) zoomPercent.getValue()).doubleValue());
        controller.setMaxIterationsMultiplier(((Number) maxIterationsMultiplier.getValue()).doubleValue());

        new Thread(() -> controller.generateNewSet()).start();
    }

    private JPanel createZoomingPanel() {
        JPanel zoomingPanel = new JPanel(new GridLayout(0, 2, 15, 2));
        zoomingPanel.setBorder(createFormPanelBorder("Zooming"));

        NumberFormat doubleFormat = NumberFormat.getNumberInstance();
        doubleFormat.setMinimumFractionDigits(1);
        doubleFormat.setMaximumFractionDigits(Double.MAX_EXPONENT);

        zoomPercent = createFieldAndLabel(doubleFormat, zoomingPanel, "Zoom:");
        zoomPercent.setToolTipText("Sets how much will it zoom in/out with every frame (Negative for zooming out)");
        maxIterationsMultiplier = createFieldAndLabel(doubleFormat, zoomingPanel, "Max iterations multiplier:");
        maxIterationsMultiplier.setToolTipText("Sets how fast will max iterations increase while zooming in. " +
                "Affects both coloring and computation speed (Higher = slower)");

        return zoomingPanel;
    }

    private JPanel createSavePanel() {
        JPanel savePanel = new JPanel();
        savePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        saveButton = new JButton("Save settings");
        saveButton.addActionListener((e) -> flushValues());
        savePanel.add(saveButton);

        return savePanel;
    }
}
