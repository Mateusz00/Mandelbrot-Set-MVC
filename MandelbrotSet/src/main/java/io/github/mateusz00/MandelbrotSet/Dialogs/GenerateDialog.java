package io.github.mateusz00.MandelbrotSet.Dialogs;

import io.github.mateusz00.MandelbrotSet.MandelbrotSetController;

import javax.swing.*;
import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;

import static io.github.mateusz00.MandelbrotSet.Utilities.SwingUtility.createFieldAndLabel;

public class GenerateDialog extends MandelbrotSetDialog
{
    private JFormattedTextField sizeY;
    private JFormattedTextField sizeX;

    /**
     * Only adds some panels and components. Does not set default close operation, resizability, visibility etc.
     */
    public GenerateDialog(JFrame mainWindow, String title, boolean modal, MandelbrotSetController controller) {
        super(mainWindow, title, modal, controller);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMinimumIntegerDigits(1);
        decimalFormat.setMaximumIntegerDigits(19);
        decimalFormat.setMaximumFractionDigits(0);
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);

        addToMainPanel(createSizePanel(decimalFormat));
    }

    @Override
    protected void flushValues() {
        super.flushValues();
        getController().setMandelbrotSize(new Dimension(getSizeXValue(), getSizeYValue()));
    }

    @Override
    protected void loadCurrentValues() {
        super.loadCurrentValues();
        setSizeXValue(getController().getMandelbrotSize().width);
        setSizeYValue(getController().getMandelbrotSize().height);
    }

    private JPanel createSizePanel(Format decimalFormat) {
        JPanel panelSize = new JPanel(new GridLayout(1, 2, 5, 5));
        panelSize.setBorder(createFormPanelBorder("File resolution"));
        JPanel subPanelCenter1 = new JPanel(new BorderLayout(5, 5));
        JPanel subPanelCenter2 = new JPanel(new BorderLayout(5, 5));

        sizeX = createFieldAndLabel(decimalFormat, subPanelCenter1, "x:", BorderLayout.WEST, BorderLayout.CENTER);
        sizeY = createFieldAndLabel(decimalFormat, subPanelCenter2, "y:", BorderLayout.WEST, BorderLayout.CENTER);

        panelSize.add(subPanelCenter1);
        panelSize.add(subPanelCenter2);

        return panelSize;
    }

    protected int getSizeXValue() {
        return ((Number) sizeX.getValue()).intValue();
    }

    protected int getSizeYValue() {
        return ((Number) sizeY.getValue()).intValue();
    }

    protected void setSizeXValue(int value) {
        sizeX.setValue(value);
    }

    protected void setSizeYValue(int value) {
        sizeY.setValue(value);
    }
}
