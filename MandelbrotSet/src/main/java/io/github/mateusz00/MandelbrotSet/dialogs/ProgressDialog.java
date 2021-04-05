package io.github.mateusz00.MandelbrotSet.dialogs;

import io.github.mateusz00.MandelbrotSet.utilities.SwingUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProgressDialog extends JDialog
{
    private final JProgressBar progressBar;
    private final JLabel processInfo = new JLabel();
    private SwingWorker<Void, Void> currentTask = null;

    public ProgressDialog(Window parentWindow) {
        super(parentWindow, "Progress info");
        setLayout(new FlowLayout());
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 8, 10));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMaximum(100);

        mainPanel.add(processInfo);
        mainPanel.add(progressBar);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

                currentTask.cancel(false);
                dispose();
            }
        });

        add(mainPanel);
        pack();
        SwingUtility.centerComponent(parentWindow.getLocationOnScreen(), parentWindow.getSize(), this);
        setModal(true);
    }

    public void executeTask(SwingWorker<Void, Void> task, String description) {
        SwingUtilities.invokeLater(() -> {
            processInfo.setText(description);
            progressBar.setValue(0);

            currentTask = task;
            task.addPropertyChangeListener((evt) -> {
                if(evt.getPropertyName() == "progress") {
                    int progress = (Integer) evt.getNewValue();
                    progressBar.setValue(progress);
                }
            });
            task.execute();
        });
    }
}
