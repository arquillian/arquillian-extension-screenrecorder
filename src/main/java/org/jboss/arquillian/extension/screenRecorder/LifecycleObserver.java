/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.arquillian.extension.screenRecorder;

import org.jboss.arquillian.extension.screenRecorder.properties.SystemProperties;
import org.jboss.arquillian.extension.screenRecorder.properties.RecorderConfiguration;
import org.jboss.arquillian.extension.screenRecorder.properties.RecordingType;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    
    private boolean screenshotsEnabled;
    private boolean videoEnabled;
    private RecordingType recordingType;

    public void initConfiguration(@Observes ArquillianDescriptor descriptor) throws IOException {
        configuration = new RecorderConfiguration();
        for (ExtensionDef extension : descriptor.getExtensions()) {
            if (extension.getExtensionName().equals(SystemProperties.SCREEN_RECORDER)) {
                configuration.setProperties(extension.getExtensionProperties());
            }
        }
        videoEnabled = configuration.isVideoEnabled();
        screenshotsEnabled = configuration.isScreenshotEnabled();
        recordingType = configuration.getRecordingType();

        FileUtils.deleteDirectory(configuration.getRootFolder());

        if (videoEnabled) {
            configuration.getVideoFolder().mkdirs();
        }
        if (screenshotsEnabled) {
            configuration.getScreenshotFolder().mkdirs();
        }
    }

    public void executeBeforeStart(@Observes AfterDeploy event) {
        if (videoEnabled && configuration.getRecordingType() == RecordingType.SUITE) {
            startRecording(configuration.getVideoFolder(), configuration.getVideoName());
        }
    }

    public void executeBeforeStop(@Observes AfterUnDeploy event) throws FileNotFoundException {
        if (videoEnabled && configuration.getRecordingType() == RecordingType.SUITE) {
            recorder.stopRecording();
        }
        deleteEmptyFolders(configuration.getRootFolder().getAbsolutePath());
    }

    public void executeBeforeTest(@Observes final Before event) throws AWTException, IOException {
        String methodName = event.getTestMethod().getName();
        timer = new Timer();
        timer.schedule(new TestTimeoutTask(methodName), 1000 * configuration.getTestTimeout());
        if (recordingType == RecordingType.FAILURE || recordingType == RecordingType.TEST) {
            File testClassDirectory = prepareDirectory(configuration.getVideoFolder(), event.getTestClass(), true);
            startRecording(testClassDirectory, methodName);
        }
        if (screenshotsEnabled && (recordingType == RecordingType.SUITE || recordingType == RecordingType.TEST)) {
            takeScreenshot(event.getTestClass(), event.getTestMethod().getName(), "before");
        }
    }

    public void executeAfterTest(@Observes After event) throws AWTException, IOException {
        timer.cancel();
        if (videoEnabled && recordingType != RecordingType.SUITE) {
            recorder.stopRecording();
            if (testResult.get().getStatus() != TestResult.Status.FAILED && recordingType == RecordingType.FAILURE) {
                recorder.stopRecording();
                File videoToDelete = FileUtils.getFile(prepareDirectory(configuration.getVideoFolder(), event.getTestClass(), false),
                        event.getTestMethod().getName() + "." + configuration.getVideoFileType());
                if (!videoToDelete.delete()) {
                    logger.warn("Temporary video file with name {} failed to delete", videoToDelete.getAbsolutePath());
                }
            }
        }
        if (screenshotsEnabled) {
            if (testResult.get().getStatus() == TestResult.Status.FAILED && recordingType == RecordingType.FAILURE) {
                takeScreenshot(event.getTestClass(), event.getTestMethod().getName(), "fail");
            } else if (recordingType != RecordingType.FAILURE) {
                takeScreenshot(event.getTestClass(), event.getTestMethod().getName(), "after");
            }
        }
    }

    private File prepareDirectory(File root, TestClass clazz, boolean create) {
        String packageName = clazz.getJavaClass().getPackage().getName();
        String className = clazz.getJavaClass().getSimpleName();
        File directory = FileUtils.getFile(root, packageName, className);
        if (create) {
            directory.mkdirs();
        }
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
        File testClassDirectory = prepareDirectory(configuration.getScreenshotFolder(), testClass, true);
        File outputFile = FileUtils.getFile(testClassDirectory, imageName);
        ImageIO.write(image, configuration.getImageFileType().toString(), outputFile);
    }

    private void deleteEmptyFolders(String folderName) {
        File aStartingDir = new File(folderName);
        List<File> emptyFolders = new ArrayList<File>();
        findEmptyFoldersInDir(aStartingDir, emptyFolders);
        List<String> fileNames = new ArrayList<String>();
        for (File f : emptyFolders) {
            String s = f.getAbsolutePath();
            fileNames.add(s);
        }
        for (File f : emptyFolders) {
            if(f.delete()) {
                logger.info("Deleted folder {} during cleanup", f.getAbsolutePath());
            }
        }
    }

    private boolean findEmptyFoldersInDir(File folder, List<File> emptyFolders) {
        boolean isEmpty = false;
        File[] filesAndDirs = folder.listFiles();
        List<File> filesDirs = Arrays.asList(filesAndDirs);
        if (filesDirs.isEmpty()) {
            isEmpty = true;
        }
        if (filesDirs.size() > 0) {
            boolean allDirsEmpty = true;
            boolean noFiles = true;
            for (File file : filesDirs) {
                if (!file.isFile()) {
                    boolean isEmptyChild = findEmptyFoldersInDir(file, emptyFolders);
                    if (!isEmptyChild) {
                        allDirsEmpty = false;
                    }
                }
                if (file.isFile()) {
                    noFiles = false;
                }
            }
            if (noFiles == true && allDirsEmpty == true) {
                isEmpty = true;
            }
        }
        if (isEmpty) {
            emptyFolders.add(folder);
        }
        return isEmpty;
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
