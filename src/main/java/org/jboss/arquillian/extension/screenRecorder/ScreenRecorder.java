/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.extension.screenRecorder;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;


/**
 *
 * @author pmensik
 */
public class ScreenRecorder {

    private static final Logger logger = LoggerFactory.getLogger(ScreenRecorder.class);

    private final int FRAME_RATE;
    private final Dimension screenBounds;
    private final String videoType;
    private volatile boolean running = false;
    private volatile File destination;
    private Thread thread;

    public ScreenRecorder(int frameRate, String videoType) {
        this.FRAME_RATE = frameRate;
        this.videoType = videoType;
        screenBounds = Toolkit.getDefaultToolkit().getScreenSize();
    }

    /**
     * Starts recording a video to the temporary file. If {@link #stopRecording(java.io.File) }
     * is invoked, this method stops recording.
     */
    public void startRecording() {
        running = true;
        thread = new Thread(new Runnable() {
            public void run() {
                File output;
                try {
                    output = File.createTempFile("arquillain-screen-recorder", "." + videoType);
                    output.deleteOnExit();
                    output.createNewFile();
                } catch (IOException e) {
                    throw new IllegalStateException("Can't create a temporary file for recording.", e);
                }
                IMediaWriter writer = ToolFactory.makeWriter(output.getAbsolutePath());
                writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4,
                    screenBounds.width / 2, screenBounds.height / 2);
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
                        try {
                            if (destination != null) {
                                if (destination.exists()) {
                                    destination.delete();
                                }
                                FileUtils.moveFile(output, destination);
                            }
                        } catch (IOException e) {
                            throw new IllegalStateException("Can't move the temporary recorded content to the destination file.", e);
                        } finally {
                            output.delete();
                        }
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * Stops recording. If the destination file is specified, the recorded content
     * is moved to it. If the file is not specified, the recorded content is dropped.
     *
     * @param destination destination file
     */
    public void stopRecording(File destination) {
        this.destination = destination;
        running = false;
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
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
