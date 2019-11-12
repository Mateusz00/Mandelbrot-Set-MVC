package io.github.mateusz00.MandelbrotSet.Dialogs;

import io.github.mateusz00.MandelbrotSet.MandelbrotSetController;
import io.github.mateusz00.MandelbrotSet.Utilities.Utility;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static io.github.mateusz00.MandelbrotSet.Utilities.SwingUtility.createFieldAndLabel;

public class VideoGenerateDialog extends MandelbrotSetDialog
{
    private final MandelbrotSetController controller;
    private final JFileChooser videoFileChooser;
    private JTextField saveDestination;
    private JButton fileChooseButton;
    private JFormattedTextField frames;
    private JFormattedTextField zoomPercent;
    private JFormattedTextField maxIterationsMultiplier;
    private JCheckBox keepImages;
    private JCheckBox generateVideo;

    /**
     * Only adds some panels and components. Does not set default close operation, resizability, visibility etc.
     */
    public VideoGenerateDialog(JFrame mainWindow, MandelbrotSetController controller) {
        super(mainWindow, "Generate video", true);
        this.controller = controller;

        // Set up videoFileChooser
        ExtensionFilter WEBMExtension = new ExtensionFilter("WEBM (*.webm)", "webm");
        WEBMExtension.setEnforcedSaveExtension("webm");

        videoFileChooser = new JFileChooser();
        videoFileChooser.setAcceptAllFileFilterUsed(false);
        videoFileChooser.addChoosableFileFilter(WEBMExtension);

        // Add panels to main panel
        addToMainPanel(createFileChooserPanel());
        addToMainPanel(createVideoSettingsPanel());
        addToMainPanel(createButtonsPanel());
    }

    private JPanel createFileChooserPanel() {
        JPanel panelFileChooser = new JPanel(new BorderLayout(5, 5));
        panelFileChooser.setBorder(createFormPanelBorder("Choose destination"));

        saveDestination = new JTextField("");
        saveDestination.setEditable(false);
        panelFileChooser.add(saveDestination, BorderLayout.CENTER);

        fileChooseButton = new JButton("Save as...");
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

        return panelFileChooser;
    }

    private JPanel createVideoSettingsPanel() {
        // Create panel
        JPanel panelVideoSettings = new JPanel();
        panelVideoSettings.setBorder(createFormPanelBorder("Video settings"));
        panelVideoSettings.setLayout(new BoxLayout(panelVideoSettings, BoxLayout.PAGE_AXIS));

        // Create formats
        NumberFormat doubleFormat = NumberFormat.getNumberInstance();
        doubleFormat.setMinimumFractionDigits(1);
        doubleFormat.setMaximumFractionDigits(Double.MAX_EXPONENT);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumIntegerDigits(19);
        decimalFormat.setMaximumFractionDigits(0);
        decimalFormat.setMinimumIntegerDigits(1);
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);

        // Video settings sub panel1
        JPanel videoSubPanel1 = new JPanel(new GridLayout(0, 2, 15, 2));

        frames = createFieldAndLabel(decimalFormat, videoSubPanel1, "Frames:");
        zoomPercent = createFieldAndLabel(doubleFormat, videoSubPanel1, "Zoom:");
        zoomPercent.setToolTipText("Sets how much % will it zoom in/out with every frame (Negative for zooming out)");
        maxIterationsMultiplier = createFieldAndLabel(doubleFormat, videoSubPanel1, "Max iterations multiplier:");
        maxIterationsMultiplier.setToolTipText("Affects both coloring and computation speed (Higher = slower)");

        panelVideoSettings.add(videoSubPanel1);

        // Video settings sub panel2
        JPanel videoSubPanel2 = new JPanel();

        generateVideo = new JCheckBox("Generate video");
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
        videoSubPanel2.add(generateVideo);

        keepImages = new JCheckBox("Keep images");
        keepImages.setToolTipText("Keep generated images(every frame is saved as an image first before creating video)");
        videoSubPanel2.add(keepImages);

        panelVideoSettings.add(videoSubPanel2);
        generateVideo.setSelected(true);

        return panelVideoSettings;
    }

    private JPanel createButtonsPanel() {
        JPanel lastPanel = new JPanel();
        lastPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton currentDataGetter = new JButton("Current data");
        currentDataGetter.addActionListener((e) -> {
            setCenterXValue(controller.getCenter().getX());
            setCenterYValue(controller.getCenter().getY());
            setZoomXValue((controller.getZoom())[0]);
            setZoomYValue((controller.getZoom())[1]);
            setMaxIterationsValue(controller.getMaxIterations());
            setEscapeRadiusValue(controller.getEscapeRadius());
            setRGBPicker(controller.getCurrentRGBPicker());
            zoomPercent.setValue(controller.getZoomPercent());
            maxIterationsMultiplier.setValue(controller.getMaxIterationsMultiplier());
        });
        lastPanel.add(currentDataGetter);

        JButton generateButton = new JButton("Generate");
        generateButton.addActionListener((e) -> {
            // Save file destination have to be chosen
            if(!saveDestination.getText().isEmpty()) {
                // Get data from components and update model, view and controller
                long framesVal = ((Number) frames.getValue()).longValue();
                if(framesVal <= 0)
                    return;

                double zoomPercentOld = controller.getZoomPercent();
                double maxIterationsMultiplierOld = controller.getMaxIterationsMultiplier();

                double zoomPercentVal = ((Number) zoomPercent.getValue()).doubleValue();
                double maxIterationsMultiplierVal = ((Number) maxIterationsMultiplier.getValue()).doubleValue();
                int digits = Utility.digitsNumber(framesVal);

                controller.setEscapeRadius(getEscapeRadiusValue());
                controller.setCenter(new Point2D.Double(getCenterXValue(), getCenterYValue()));
                controller.setMaxIterations(getMaxIterationsValue());
                controller.setZoom(new double[]{getZoomXValue(), getZoomYValue()});
                controller.setRGBPicker(getRGBPicker());
                controller.setZoomPercent(zoomPercentVal);
                controller.setMaxIterationsMultiplier(maxIterationsMultiplierVal);

                // Create directory for frames
                File destination = new File(saveDestination.getText());
                File framesDir = new File(Utility.removeExtension(destination.getAbsolutePath()) + "_frames");
                framesDir.mkdirs();

                // Generate all frames and save them as individual images
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
                        JOptionPane.showMessageDialog(this, "Error: Couldn't find ffmpeg/ffprobe" +
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

                // Set some old values that were overwritten by video settings
                controller.setZoomPercent(zoomPercentOld);
                controller.setMaxIterationsMultiplier(maxIterationsMultiplierOld);
            }
            else
                JOptionPane.showMessageDialog(this, "Error: Choose save file destination!",
                        "Error", JOptionPane.ERROR_MESSAGE);
        });
        lastPanel.add(generateButton);

        return lastPanel;
    }
}
