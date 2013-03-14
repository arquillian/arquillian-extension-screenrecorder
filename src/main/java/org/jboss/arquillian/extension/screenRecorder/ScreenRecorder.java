/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.arquillian.extension.screenRecorder;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pmensik
 */
public class ScreenRecorder {

    private static final Logger logger = LoggerFactory.getLogger(ScreenRecorder.class);
    
    private final int FRAME_RATE;
    private Dimension screenBounds;
    private IMediaWriter writer;
    private boolean running = false;

    public ScreenRecorder(File file, int frameRate) {
        this.FRAME_RATE = frameRate;
        screenBounds = Toolkit.getDefaultToolkit().getScreenSize();
        writer = ToolFactory.makeWriter(file.getAbsolutePath());
        writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4,
                screenBounds.width / 2, screenBounds.height / 2);
    }

    public void startRecording() {
        running = true;
        new Thread(new Runnable() {
            public void run() {
                long startTime = System.nanoTime();
                while (running) {
                    BufferedImage screen = getDesktopScreenshot();

                    BufferedImage bgrScreen = convertToType(screen,
                            BufferedImage.TYPE_3BYTE_BGR);

                    writer.encodeVideo(0, bgrScreen, System.nanoTime() - startTime,
                            TimeUnit.NANOSECONDS);
                    try {
                        Thread.sleep((long) (1000 / FRAME_RATE));
                    } catch (InterruptedException ex) {
                        logger.error("Exception occured during video recording", ex);
                    }
                    if (!running) {
                        writer.close();
                    }
                }
            }
        }).start();
    }

    public void stopRecording() {
        running = false;
    }

    private BufferedImage convertToType(BufferedImage sourceImage, int targetType) {
        BufferedImage image;
        if (sourceImage.getType() == targetType) {
            image = sourceImage;
        } // otherwise create a new image of the target type and draw the new image
        else {
            image = new BufferedImage(sourceImage.getWidth(),
                    sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }
        return image;

    }

    private BufferedImage getDesktopScreenshot() {
        try {
            Robot robot = new Robot();
            Rectangle captureSize = new Rectangle(screenBounds);
            return robot.createScreenCapture(captureSize);
        } catch (AWTException e) {
            logger.error("Exception occured while taking screenshot for video record", e);
            return null;
        }
    }
}
