package io.github.mateusz00.MandelbrotSet.Dialogs;

import io.github.mateusz00.MandelbrotSet.MandelbrotSetController;
import io.github.mateusz00.MandelbrotSet.Utilities.Utility;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
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
    private JFormattedTextField zoomStep;
    private JFormattedTextField maxIterationsMultiplier;
    private JCheckBox keepImages;
    private JCheckBox generateVideo;

    /**
     * Only adds some panels and components. Does not set default close operation, resizability, visibility etc.
     */
    public VideoGenerateDialog(JFrame mainWindow, MandelbrotSetController controller) {
        super(mainWindow, "Generate video", true, controller);
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
        zoomStep = createFieldAndLabel(doubleFormat, videoSubPanel1, "Zoom:");
        zoomStep.setToolTipText("Sets how much will it zoom in/out with every frame (Use values bigger than 0)");
        maxIterationsMultiplier = createFieldAndLabel(doubleFormat, videoSubPanel1, "Max iterations multiplier:");
        maxIterationsMultiplier.setToolTipText("Sets how fast will max iterations increase while zooming in. " +
                "Affects both coloring and computation speed (Higher = slower)");

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

    @Override
    protected void loadCurrentValues() {
        super.loadCurrentValues();
        zoomStep.setValue(controller.getZoomStep());
        maxIterationsMultiplier.setValue(controller.getMaxIterationsMultiplier());
    }

    @Override
    protected void flushValues() {
        super.flushValues();
        controller.setZoomStep(Math.max(((Number) zoomStep.getValue()).doubleValue(), 0));
        controller.setMaxIterationsMultiplier(((Number) maxIterationsMultiplier.getValue()).doubleValue());
    }

    private JPanel createButtonsPanel() {
        JPanel lastPanel = new JPanel();
        lastPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton currentDataGetter = new JButton("Current data");
        currentDataGetter.addActionListener((e) -> loadCurrentValues());
        lastPanel.add(currentDataGetter);

        JButton generateButton = new JButton("Generate");
        generateButton.addActionListener((e) -> {
            // Save file destination have to be chosen
            if(!saveDestination.getText().isEmpty()) {
                // Get data from components and update model, view and controller
                long framesVal = ((Number) frames.getValue()).longValue();
                if(framesVal <= 0)
                    return;

                double zoomStepOld = controller.getZoomStep();
                double maxIterationsMultiplierOld = controller.getMaxIterationsMultiplier();
                int digits = Utility.digitsNumber(framesVal);

                // Update mandelbrot set model
                flushValues();

                // Create directory for frames
                File destination = new File(saveDestination.getText());
                File framesDir = new File(Utility.removeExtension(destination.getAbsolutePath()) + "_frames");
                framesDir.mkdirs();

                // Generate all frames and save them as individual images then use them to create video if user checked
                // appropriate checkbox and have ffmpeg
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.executeTask(new FrameGenerator(framesVal, digits, framesDir, progressDialog),
                        "Generating frames...");
                progressDialog.setVisible(true);

                // Set some old values that were overwritten by video settings
                controller.setZoomStep(zoomStepOld);
                controller.setMaxIterationsMultiplier(maxIterationsMultiplierOld);
            }
            else
                JOptionPane.showMessageDialog(this, "Error: Choose save file destination!",
                        "Error", JOptionPane.ERROR_MESSAGE);
        });
        lastPanel.add(generateButton);

        return lastPanel;
    }

    private class FrameGenerator extends SwingWorker<Void, Void> {
        private final long framesVal;
        private final int digits;
        private final File framesDir;
        private final ProgressDialog dialog;

        public FrameGenerator(long framesVal, int digits, File framesDir, ProgressDialog dialog) {
            this.framesVal = framesVal;
            this.digits = digits;
            this.framesDir = framesDir;
            this.dialog = dialog;
        }

        @Override
        protected Void doInBackground() {
            if(framesVal > 0) {
                // Generate mandelbrot set
                controller.generateNewSet();
                saveFrame(0);
                setProgress((int) ((1 * 100) / framesVal));

                // Generate all frames and save them as individual images
                double zoomStep = controller.getZoomStep();

                for(long i = 1; i < framesVal && !isCancelled(); ++i) {
                    setProgress((int) ((i * 100) / framesVal));
                    controller.zoom(zoomStep, true);
                    saveFrame(i);
                }
            }

            return null;
        }

        @Override
        protected void done() {
            if(isCancelled())
                return;

            // Use ffmpeg (if there is one and user has selected generate video)
            if(generateVideo.isSelected()) {
                dialog.executeTask(new EncoderTask(framesDir, dialog, digits, framesVal), "Creating video...");
            }
            else {
                dialog.dispose();
                JOptionPane.showMessageDialog(VideoGenerateDialog.this, "Finished generating frames",
                        "Task completed", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private void saveFrame(long frameNumber) {
            BufferedImage img = controller.getBufferedImage();

            // Write generated mandelbrot set to file
            String fileNumber = String.format("%0" + digits + "d", frameNumber);
            File file = new File(framesDir + "/frame" + fileNumber + ".png");

            try {
                ImageIO.write(img, "png", file);
            }
            catch(IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    private class EncoderTask extends SwingWorker<Void, Void> {
        private final File framesDir;
        private final ProgressDialog dialog;
        private final int digits;
        private final long frames;

        public EncoderTask(File framesDir, ProgressDialog dialog, int digits, long frames) {
            this.framesDir = framesDir;
            this.dialog = dialog;
            this.digits = digits;
            this.frames = frames;
        }

        @Override
        protected Void doInBackground() {
            try {
                FFmpeg ffmpeg = new FFmpeg("ffmpeg/ffmpeg");
                FFprobe ffprobe = new FFprobe("ffmpeg/ffprobe");

                FFmpegProbeResult input = ffprobe.probe(framesDir + "/frame%0" + digits + "d.png");

                FFmpegBuilder builder = new FFmpegBuilder()
                        .setInput(input)
                        .overrideOutputFiles(true)
                        .addOutput(saveDestination.getText())
                            .setFormat("webm")
                            .setVideoCodec("libvpx")
                            .setVideoFrameRate(FFmpeg.FPS_24)
                            .addExtraArgs("-fpre", "ffmpeg/libvpx.ffpreset")
                            .addExtraArgs("-quality", "best")
                            .done();

                FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
                executor.createJob(builder, (progress) -> {
                    int percentage = (int) ((progress.frame * 100) / frames);
                    setProgress(percentage);
                }).run();

                // Delete images if user didn't check keep images checkbox
                if(!keepImages.isSelected()) {
                    try {
                        FileUtils.deleteDirectory(framesDir);
                    }
                    catch(IOException ee) {
                        ee.printStackTrace();
                    }
                }
            }
            catch(IOException ee) {
                ee.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(dialog, "Error: Couldn't find ffmpeg/ffprobe " +
                            "in ffmpeg directory", "Error", JOptionPane.ERROR_MESSAGE);
                });
            }

            return null;
        }

        @Override
        protected void done() {
            if(isCancelled())
                return;

            dialog.dispose();
            JOptionPane.showMessageDialog(VideoGenerateDialog.this, "Finished creating video",
                    "Task completed", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

