package io.github.mateusz00.MandelbrotSet.dialogs;

import io.github.mateusz00.MandelbrotSet.mandelbrot.MandelbrotSetController;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageGenerateDialog extends GenerateDialog
{
    private final JFileChooser imageFileChooser;
    private JTextField saveDestination;
    private JButton fileChooseButton;

    /**
     * Only adds some panels and components. Does not set default close operation, resizability, visibility etc.
     */
    public ImageGenerateDialog(JFrame mainWindow, MandelbrotSetController controller) {
        super(mainWindow, "Generate image", true, controller);

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
                // Save values that will be restored after generating image
                int sizeX = getController().getMandelbrotSize().width;
                int sizeY = getController().getMandelbrotSize().height;

                // Generate mandelbrot set
                flushValues();
                getController().generateNewSet();
                BufferedImage img = getController().getBufferedImage();

                // Write generated mandelbrot set to file
                File file = new File(saveDestination.getText());

                try {
                    String extension = ((ExtensionFilter) imageFileChooser.getFileFilter()).getEnforcedSaveExtension();
                    ImageIO.write(img, extension, file);

                    JOptionPane.showMessageDialog(this, "Finished generating image",
                            "Task completed", JOptionPane.INFORMATION_MESSAGE);
                }
                catch(IOException exception) {
                    exception.printStackTrace();

                    JOptionPane.showMessageDialog(this, "Couldn't save image",
                            "Failure", JOptionPane.ERROR_MESSAGE);
                }
                finally {
                    getController().setMandelbrotSize(new Dimension(sizeX, sizeY));
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
