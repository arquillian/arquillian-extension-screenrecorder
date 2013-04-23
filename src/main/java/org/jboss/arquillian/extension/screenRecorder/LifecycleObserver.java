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
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.extension.screenRecorder.properties.RecorderConfiguration;
import org.jboss.arquillian.extension.screenRecorder.properties.RecordingType;
import org.jboss.arquillian.extension.screenRecorder.properties.SystemProperties;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pmensik
 */
public class LifecycleObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleObserver.class);

    @Inject
    Instance<TestResult> testResult;

    private RecorderConfiguration configuration;
    private ScreenRecorder recorder;
    private Timer timer;

    private File screenshotBefore;

    public void initConfiguration(@Observes ArquillianDescriptor descriptor) throws IOException {
        configuration = new RecorderConfiguration();
        for (ExtensionDef extension : descriptor.getExtensions()) {
            if (extension.getExtensionName().equals(SystemProperties.SCREEN_RECORDER)) {
                configuration.setProperties(extension.getExtensionProperties());
            }
        }
        FileUtils.deleteDirectory(configuration.getRootFolder());

        if (!configuration.getVideoRecordingType().equals(RecordingType.NONE)) {
            configuration.getVideoFolder().mkdirs();
            timer = new Timer();
            timer.schedule(new TestTimeoutTask(), TimeUnit.SECONDS.toMillis(configuration.getTestTimeout()));
        }
        if (!configuration.getScreenshotRecordingType().equals(RecordingType.NONE)) {
            configuration.getScreenshotFolder().mkdirs();
        }
    }

    public void executeBeforeStart(@Observes AfterDeploy event) {
        if (configuration.getVideoRecordingType().equals(RecordingType.SUITE)) {
            startRecording(configuration.getVideoFolder(), configuration.getVideoName());
        }
    }

    public void executeBeforeStop(@Observes AfterUnDeploy event) {
        if (configuration.getVideoRecordingType().equals(RecordingType.SUITE)) {
            stopRecording(configuration.getVideoFolder(), configuration.getVideoName());
        }
    }

    public void executeBeforeTest(@Observes final Before event) throws AWTException, IOException {
        if (configuration.getVideoRecordingType().equals(RecordingType.TEST) || configuration.getVideoRecordingType().equals(RecordingType.FAILURE)) {
            timer = new Timer();
            timer.schedule(new TestTimeoutTask(event.getTestClass(), event.getTestMethod()), TimeUnit.SECONDS.toMillis(configuration.getTestTimeout()));
            File testClassDirectory = getDirectory(configuration.getVideoFolder(), event.getTestClass());
            startRecording(testClassDirectory, event.getTestMethod().getName());
        } else {
            timer = new Timer();
            timer.schedule(new TestTimeoutTask(), TimeUnit.SECONDS.toMillis(configuration.getTestTimeout()));
        }
        if (configuration.getScreenshotRecordingType().equals(RecordingType.TEST) || configuration.getScreenshotRecordingType().equals(RecordingType.FAILURE)) {
            screenshotBefore = takeScreenshot();
        }
    }

    public void executeAfterTest(@Observes After event) throws AWTException, IOException {
        switch(configuration.getVideoRecordingType()) {
            case FAILURE:
                if (!testResult.get().getStatus().equals(TestResult.Status.FAILED)) {
                    recorder.stopRecording(null);
                    break;
                }
            case TEST:
                if (testResult.get().getStatus().equals(TestResult.Status.SKIPPED)) {
                    recorder.stopRecording(null);
                    break;
                }
                File testClassDirectory = getDirectory(configuration.getVideoFolder(), event.getTestClass());
                stopRecording(testClassDirectory, event.getTestMethod().getName() + "_" + testResult.get().getStatus().name().toLowerCase());
                break;
        }
        switch(configuration.getScreenshotRecordingType()) {
            case FAILURE:
                if (!testResult.get().getStatus().equals(TestResult.Status.FAILED)) {
                    screenshotBefore.delete();
                    break;
                }
            case TEST:
                if (testResult.get().getStatus().equals(TestResult.Status.SKIPPED)) {
                    screenshotBefore.delete();
                    break;
                }
                takeScreenshot(screenshotBefore, event.getTestClass(), event.getTestMethod(), testResult.get().getStatus().name().toLowerCase());
        }
    }

    protected File getDirectory(File root, TestClass clazz) {
        String packageName = clazz.getJavaClass().getPackage().getName();
        String className = clazz.getJavaClass().getSimpleName();
        File directory = FileUtils.getFile(root, packageName, className);
        return directory;
    }

    private void startRecording(File directory, String fileName) {
        recorder = new ScreenRecorder(configuration.getFrameRate(), configuration.getVideoFileType());
        recorder.startRecording();
    }

    private synchronized void stopRecording(File directory, String fileName) {
        directory.mkdirs();
        String videoName = fileName + "." + configuration.getVideoFileType();
        File video = FileUtils.getFile(directory, videoName);
        recorder.stopRecording(video);
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * Moves already taken screenshot and uses it as a 'before' one. Then
     * takes another screenshot.
     */
    private void takeScreenshot(File before, TestClass testClass, Method testMethod, String appender) throws AWTException, IOException {
        File testClassDirectory = getDirectory(configuration.getScreenshotFolder(), testClass);
        testClassDirectory.mkdirs();

        FileUtils.moveFile(before, FileUtils.getFile(testClassDirectory, testMethod.getName() + "_before." + configuration.getImageFileType()));

        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage image = new Robot().createScreenCapture(screenSize);
        String imageName = testMethod.getName() + "_" + appender + "." + configuration.getImageFileType();

        File outputFile = FileUtils.getFile(testClassDirectory, imageName);
        ImageIO.write(image, configuration.getImageFileType().toString(), outputFile);
    }

    /**
     * Takes screenshot and saves it into a temporary file.
     */
    private File takeScreenshot() throws AWTException, IOException {
        Rectangle screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage image = new Robot().createScreenCapture(screenSize);
        File temp = File.createTempFile("arquillian-screen-recorder", "." + configuration.getImageFileType());
        temp.deleteOnExit();
        ImageIO.write(image, configuration.getImageFileType().toString(), temp);
        return temp;
    }

    private class TestTimeoutTask extends TimerTask {

        private final TestClass testClass;
        private final Method method;

        public TestTimeoutTask() {
            this.testClass = null;
            this.method = null;
        }

        public TestTimeoutTask(TestClass testClass, Method method) {
            this.testClass = testClass;
            this.method = method;
        }

        @Override
        public void run() {
            if (testClass != null && method != null) {
                stopRecording(getDirectory(configuration.getVideoFolder(), testClass), method.getName() + "_timeout");
                LOGGER.error("Test method {} in class {} has reached its timeout, stopping video recording", method.getName(), testClass.getName());
            } else {
                stopRecording(configuration.getVideoFolder(), configuration.getVideoName());
                LOGGER.error("The last test method reached its timeout, stopping video recording.");
            }
        }
    }
}
