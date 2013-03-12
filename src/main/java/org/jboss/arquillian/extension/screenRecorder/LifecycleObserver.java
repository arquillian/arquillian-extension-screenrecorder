/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.arquillian.extension.screenRecorder;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;

import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;


/**
 *
 * @author pmensik
 */
public class LifecycleObserver {

    
    private RecorderConfiguration configuration;
    private ScreenRecorder recorder;
    
    private boolean recordEachTestSeparately;
    private boolean shouldTakeScreenshots;
    private boolean shouldRecordVideo;

    public void initConfiguration(@Observes ArquillianDescriptor descriptor) throws IOException {
        configuration = new RecorderConfiguration();
        for (ExtensionDef extension : descriptor.getExtensions()) {
            if (extension.getExtensionName().equalsIgnoreCase(SystemProperties.SCREEN_RECORDER)) {
                configuration.setProperties(extension.getExtensionProperties());
            }
        }
        recordEachTestSeparately = configuration.isEachTestRecordedSeparately();
        shouldRecordVideo = configuration.shouldRecordVideo();
        shouldTakeScreenshots = configuration.shouldTakeScreenshots();

        FileUtils.deleteDirectory(new File(configuration.getRootFolder()));

        if (shouldRecordVideo) {
            new File(configuration.getVideoFolder()).mkdirs();
        }
        if (shouldTakeScreenshots) {
            new File(configuration.getScreenshotFolder()).mkdirs();
        }
    }

    public void executeBeforeStart(@Observes AfterDeploy event) {
        if (!recordEachTestSeparately && shouldRecordVideo) {
            startRecording(configuration.getVideoName());
        }
    }

    public void executeBeforeStop(@Observes AfterUnDeploy event) {
        if (!recordEachTestSeparately && shouldRecordVideo) {
            recorder.stopRecording();
        }
    }

    public void executeBeforeTest(@Observes Before event) throws AWTException, IOException {
        //timer task
        if (recordEachTestSeparately) {
            startRecording(event.getTestMethod().getName());
        }
        if (shouldTakeScreenshots) {
            createScreenshotAndSaveFile(event.getTestMethod().getName(), "before");
        }
    }

    public void executeAfterTest(@Observes After event) throws AWTException, IOException {
        if (recordEachTestSeparately) {
            recorder.stopRecording();
        }
        if (shouldTakeScreenshots) {
            createScreenshotAndSaveFile(event.getTestMethod().getName(), "after");
        }
    }

    private void startRecording(String fileName) {
        String videoName = fileName + "." + configuration.getVideoFileType();
        File video = FileUtils.getFile(configuration.getVideoFolder(), videoName);
        recorder = new ScreenRecorder(video, configuration.getFrameRate());
        recorder.startRecording();
    }

    private void createScreenshotAndSaveFile(String testName, String when) throws AWTException, IOException {
        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage image = new Robot().createScreenCapture(screenSize);

        String imageName = testName + "_" + when + "." + configuration.getImageFileType();
        File outputFile = FileUtils.getFile(configuration.getScreenshotFolder(), imageName);
        ImageIO.write(image, configuration.getImageFileType(), outputFile);
    }
}
