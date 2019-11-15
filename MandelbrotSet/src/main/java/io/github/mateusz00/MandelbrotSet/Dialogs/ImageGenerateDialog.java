package io.github.mateusz00.MandelbrotSet.Dialogs;

import io.github.mateusz00.MandelbrotSet.MandelbrotSetController;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageGenerateDialog extends MandelbrotSetDialog
{
    private final MandelbrotSetController controller;
    private final JFileChooser imageFileChooser;
    private JTextField saveDestination;
    private JButton fileChooseButton;

    /**
     * Only adds some panels and components. Does not set default close operation, resizability, visibility etc.
     */
    public ImageGenerateDialog(JFrame mainWindow, MandelbrotSetController controller) {
        super(mainWindow, "Generate image", true, controller);
        this.controller = controller;

        // Set up imageFileChooser
        ExtensionFilter PNGExtension = new ExtensionFilter("PNG (*.png)", "png");
        ExtensionFilter JPGExtension = new ExtensionFilter("JPG (*.jpg)", "jpg");
        PNGExtension.setEnforcedSaveExtension("png");
        JPGExtension.setEnforcedSaveExtension("jpg");

        imageFileChooser = new JFileChooser();
        imageFileChooser.setAcceptAllFileFilterUsed(false);
        imageFileChooser.addChoosableFileFilter(PNGExtension);
        imageFileChooser.addChoosableFileFilter(JPGExtension);

        // Add panels
        addToMainPanel(createFileChooserPanel());
        addToMainPanel(createButtonsPanel());
    }

    private JPanel createFileChooserPanel() {
        JPanel panelFileChooser = new JPanel(new BorderLayout(5, 5));
        panelFileChooser.setBorder(createFormPanelBorder("Choose destination"));

        // File chooser
        saveDestination = new JTextField("", 16);
        saveDestination.setEditable(false);
        panelFileChooser.add(saveDestination, BorderLayout.CENTER);

        fileChooseButton = new JButton("Save as...");
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

        return panelFileChooser;
    }

    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton currentDataGetter = new JButton("Current data");
        currentDataGetter.addActionListener((e) -> loadCurrentValues());
        buttonsPanel.add(currentDataGetter);

        JButton generateButton = new JButton("Generate");
        generateButton.addActionListener((e) -> {
            // Save file destination have to be chosen
            if(!saveDestination.getText().isEmpty()) {
                // Generate mandelbrot set
                flushValues();
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
                JOptionPane.showMessageDialog(this, "Error: Choose save file destination!",
                        "Error", JOptionPane.ERROR_MESSAGE);
        });
        buttonsPanel.add(generateButton);

        return buttonsPanel;
    }
}
