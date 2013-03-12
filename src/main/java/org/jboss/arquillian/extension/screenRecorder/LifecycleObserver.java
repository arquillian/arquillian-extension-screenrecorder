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
import java.lang.reflect.Method;

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

/**
 *
 * @author pmensik
 */
public class LifecycleObserver {

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
        timer = new Timer();
        timer.schedule(new TestTimeoutTask(), 1000 * configuration.getTestTimeout());
        if (recordEachTestSeparately) {
            File testClassDirectory = prepareDirectory(configuration.getVideoFolder(), event.getTestClass());
            startRecording(testClassDirectory, event.getTestMethod().getName());
        }
        if (shouldTakeScreenshots && !shouldTakeScreenshotsOnlyOnFail) {
            takeScreenshot(event.getTestClass(), event.getTestMethod(), "before");
        }
    }

    public void executeAfterTest(@Observes After event) throws AWTException, IOException {
        timer.cancel();
        if (recordEachTestSeparately) {
            recorder.stopRecording();
        }
        if (shouldTakeScreenshots) {
            takeScreenshot(event.getTestClass(), event.getTestMethod(), "after");
        } else if(shouldTakeScreenshotsOnlyOnFail) {
            if(testResult.get().getStatus() == TestResult.Status.FAILED){
                takeScreenshot(event.getTestClass(), event.getTestMethod(), "fail");
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

    private void takeScreenshot(TestClass testClass, Method testMethod, String appender) throws AWTException, IOException {
        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage image = new Robot().createScreenCapture(screenSize);

        String imageName = testMethod.getName() + "_" + appender + "." + configuration.getImageFileType();
        File testClassDirectory = prepareDirectory(configuration.getScreenshotFolder(), testClass);
        File outputFile = FileUtils.getFile(testClassDirectory, imageName);
        ImageIO.write(image, configuration.getImageFileType().toString(), outputFile);
    }
    
    private class TestTimeoutTask extends TimerTask {
        
        @Override
        public void run() {
            recorder.stopRecording();
        }
    }
}
