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
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pmensik
 */
public class LifecycleObserver {

    private static final Logger logger = LoggerFactory.getLogger(LifecycleObserver.class);
    
    @Inject
    Instance<TestResult> testResult;
    
    private RecorderConfiguration configuration;
    private ScreenRecorder recorder;
    
    private Timer timer;
    
    private boolean recordEachTestSeparately;
    private boolean shouldTakeScreenshots;
    private boolean shouldRecordVideo;
    private boolean shouldTakeScreenshotsOnlyOnFail;

    public void initConfiguration(@Observes ArquillianDescriptor descriptor) throws IOException {
        configuration = new RecorderConfiguration();
        for (ExtensionDef extension : descriptor.getExtensions()) {
            if (extension.getExtensionName().equals(SystemProperties.SCREEN_RECORDER)) {
                configuration.setProperties(extension.getExtensionProperties());
            }
        }
        recordEachTestSeparately = configuration.isEachTestRecordedSeparately();
        shouldRecordVideo = configuration.shouldRecordVideo();
        shouldTakeScreenshots = configuration.shouldTakeScreenshots();
        shouldTakeScreenshotsOnlyOnFail = configuration.shouldTakeScreenshotsOnlyOnFail();

        FileUtils.deleteDirectory(configuration.getRootFolder());

        if (shouldRecordVideo) {
            configuration.getVideoFolder().mkdirs();
        }
        if (shouldTakeScreenshots || shouldTakeScreenshotsOnlyOnFail) {
            configuration.getScreenshotFolder().mkdirs();
        }
    }

    public void executeBeforeStart(@Observes AfterDeploy event) {
        if (!recordEachTestSeparately && shouldRecordVideo) {
            startRecording(configuration.getVideoFolder(), configuration.getVideoName());
        }
    }

    public void executeBeforeStop(@Observes AfterUnDeploy event) {
        if (!recordEachTestSeparately && shouldRecordVideo) {
            recorder.stopRecording();
        }
    }

    public void executeBeforeTest(@Observes final Before event) throws AWTException, IOException {
        String methodName = event.getTestMethod().getName();
        timer = new Timer();
        timer.schedule(new TestTimeoutTask(methodName), 1000 * configuration.getTestTimeout());
        if (recordEachTestSeparately) {
            File testClassDirectory = prepareDirectory(configuration.getVideoFolder(), event.getTestClass());
            startRecording(testClassDirectory, methodName);
        }
        if (shouldTakeScreenshots && !shouldTakeScreenshotsOnlyOnFail) {
            takeScreenshot(event.getTestClass(), methodName, "before");
        }
    }

    public void executeAfterTest(@Observes After event) throws AWTException, IOException {
        timer.cancel();
        if (recordEachTestSeparately) {
            recorder.stopRecording();
        }
        if (shouldTakeScreenshots) {
            takeScreenshot(event.getTestClass(), event.getTestMethod().getName(), "after");
        } else if (shouldTakeScreenshotsOnlyOnFail) {
            if (testResult.get().getStatus() == TestResult.Status.FAILED) {
                takeScreenshot(event.getTestClass(), event.getTestMethod().getName(), "fail");
            }
        }
    }

    protected File prepareDirectory(File root, TestClass clazz) {
        String packageName = clazz.getJavaClass().getPackage().getName();
        String className = clazz.getJavaClass().getSimpleName();
        File directory = FileUtils.getFile(root, packageName, className);
        directory.mkdirs();
        return directory;
    }

    private void startRecording(File directory, String fileName) {
        String videoName = fileName + "." + configuration.getVideoFileType();
        File video = FileUtils.getFile(directory, videoName);
        recorder = new ScreenRecorder(video, configuration.getFrameRate());
        recorder.startRecording();
    }

    private void takeScreenshot(TestClass testClass, String methodName, String appender) throws AWTException, IOException {
        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage image = new Robot().createScreenCapture(screenSize);

        String imageName = methodName + "_" + appender + "." + configuration.getImageFileType();
        File testClassDirectory = prepareDirectory(configuration.getScreenshotFolder(), testClass);
        File outputFile = FileUtils.getFile(testClassDirectory, imageName);
        ImageIO.write(image, configuration.getImageFileType().toString(), outputFile);
    }

    private class TestTimeoutTask extends TimerTask {

        private String testMethodName;

        public TestTimeoutTask(String testMethodName) {
            this.testMethodName = testMethodName;
        }

        @Override
        public void run() {
            recorder.stopRecording();
            logger.error("Test method {} has reached its timeout, stopping video recording", testMethodName);
        }
    }
}
