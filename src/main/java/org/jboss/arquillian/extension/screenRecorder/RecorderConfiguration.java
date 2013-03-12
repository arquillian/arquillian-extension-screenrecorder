/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.arquillian.extension.screenRecorder;

import java.io.File;
import java.util.Map;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author pmensik
 */
public class RecorderConfiguration {

    /* Defaults */
    private static final String DEFAULT_ROOT_FOLDER = "target/media";
    private static final String DEFAULT_VIDEO_FOLDER = "video";
    private static final String DEFAULT_SCREENSHOT_FOLDER = "screenshot";
    private static final String DEFAULT_VIDEO_NAME = "record";
    private static final ImageType DEFAULT_IMAGE_FILE_TYPE = ImageType.PNG;
    private static final String DEFAULT_VIDEO_FILE_TYPE = "mp4";
    private static final boolean DEFAULT_RECORD_TESTS_SEPARATELY = false;
    private static final boolean DEFAULT_SHOULD_TAKE_SCREENSHOTS = true;
    private static final boolean DEFAULT_SHOULD_RECORD_VIDEO = true;
    private static final int DEFAULT_FRAME_RATE = 20;
    private static final int DEFAULT_TEST_TIMEOUT = 100;

    private Map<String, String> properties;

    public File getVideoFolder() {
        String folder = isPropertyExists(SystemProperties.VIDEO_FOLDER) ?
                properties.get(SystemProperties.VIDEO_FOLDER) : DEFAULT_VIDEO_FOLDER;
        return FileUtils.getFile(getRootFolder(), folder);
    }

    public File getScreenshotFolder() {
        String folder = isPropertyExists(SystemProperties.SCREENSHOT_FOLDER) ?
                properties.get(SystemProperties.SCREENSHOT_FOLDER) : DEFAULT_SCREENSHOT_FOLDER;
        return FileUtils.getFile(getRootFolder(), folder);
    }

    public File getRootFolder() {
        return new File(isPropertyExists(SystemProperties.MEDIA_ROOT) ?
                properties.get(SystemProperties.MEDIA_ROOT) : DEFAULT_ROOT_FOLDER);
    }

    public String getVideoName() {
        return isPropertyExists(SystemProperties.VIDEO_NAME) ?
                properties.get(SystemProperties.VIDEO_NAME) : DEFAULT_VIDEO_NAME;
    }

    public ImageType getImageFileType() {
        return isPropertyExists(SystemProperties.IMAGE_FILE_TYPE) ?
                ImageType.valueOf(properties.get(SystemProperties.IMAGE_FILE_TYPE)) : DEFAULT_IMAGE_FILE_TYPE;
    }

    public boolean isEachTestRecordedSeparately() {
        return isPropertyExists(SystemProperties.RECORD_TESTS_SEPARATELY) ?
                Boolean.parseBoolean(properties.get(SystemProperties.RECORD_TESTS_SEPARATELY)) : DEFAULT_RECORD_TESTS_SEPARATELY;
    }

    public boolean shouldTakeScreenshots() {
        return isPropertyExists(SystemProperties.SHOULD_TAKE_SCREENSHOTS) ?
                Boolean.parseBoolean(properties.get(SystemProperties.SHOULD_TAKE_SCREENSHOTS)) : DEFAULT_SHOULD_TAKE_SCREENSHOTS;
    }

    public boolean shouldRecordVideo() {
        return isPropertyExists(SystemProperties.SHOULD_RECORD_VIDEO) ?
                Boolean.parseBoolean(properties.get(SystemProperties.SHOULD_RECORD_VIDEO)) : DEFAULT_SHOULD_RECORD_VIDEO;
    }

    public int getFrameRate() {
        return isPropertyExists(SystemProperties.FRAME_RATE) ?
                Integer.parseInt(properties.get(SystemProperties.FRAME_RATE)) : DEFAULT_FRAME_RATE;
    }

    public int getTestTimeout() {
        return isPropertyExists(SystemProperties.TEST_TIMEOUT) ?
                Integer.parseInt(properties.get(SystemProperties.TEST_TIMEOUT)) : DEFAULT_TEST_TIMEOUT;
    }

    public String getVideoFileType() {
        return DEFAULT_VIDEO_FILE_TYPE;
    }

    private boolean isPropertyExists(String property) {
        if(properties == null) {
            return false;
        } else if(properties.get(property) == null || properties.get(property).isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }


}
