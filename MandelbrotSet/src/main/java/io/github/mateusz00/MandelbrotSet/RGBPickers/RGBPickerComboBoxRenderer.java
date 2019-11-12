package io.github.mateusz00.MandelbrotSet.RGBPickers;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

public class RGBPickerComboBoxRenderer extends BasicComboBoxRenderer
{
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setText(((RGBPicker) value).getDescription());

        return this;
    }
}

