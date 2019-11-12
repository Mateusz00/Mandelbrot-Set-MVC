package io.github.mateusz00.MandelbrotSet.Utilities;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.Format;

public class SwingUtility
{
    /**
     * Creates FormattedTextField and label and adds them to specified panel
     * @return Created JFormattedTextField
     */
    public static JFormattedTextField createFieldAndLabel(Format format, JPanel panel, String label) {
        return createFieldAndLabel(format, panel, label, null, null);
    }

    /**
     * Creates FormattedTextField and label and adds them to specified panel
     * @return Created JFormattedTextField
     */
    public static JFormattedTextField createFieldAndLabel(Format format, JPanel panel, String label, Object labelConstraints,
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

    public static Border getFlowLayoutDefaultPadding() {
        return BorderFactory.createEmptyBorder(5, 5, 5, 5);
    }

    /**
     * @param parentPosition position of the component from which function will get center
     * @param parentSize size of the component from which function will get center
     * @param component component that you want to center
     */
    public static void centerComponent(Point parentPosition, Dimension parentSize, Component component) {
        int xDifference = (parentSize.width / 2) - (component.getWidth() / 2);
        int yDifference = (parentSize.height / 2) - (component.getHeight() / 2);

        component.setLocation(parentPosition.x + xDifference, parentPosition.y + yDifference);
    }
}
