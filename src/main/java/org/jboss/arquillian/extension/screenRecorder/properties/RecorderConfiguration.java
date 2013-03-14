/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.arquillian.extension.screenRecorder.properties;

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
    private static final boolean DEFAULT_SCREENSHOTS_ENABLED = true;
    private static final boolean DEFAULT_VIDEO_ENABLED = true;
    private static final RecordingType DEFAULT_RECORDING_TYPE = RecordingType.FAILURE;
    private static final int DEFAULT_FRAME_RATE = 20;
    private static final int DEFAULT_TEST_TIMEOUT = 3600; //one hour
    
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
        return new File(isPropertyExists(SystemProperties.ROOT_FOLDER) ?
                properties.get(SystemProperties.ROOT_FOLDER) : DEFAULT_ROOT_FOLDER);
    }

    public String getVideoName() {
        return isPropertyExists(SystemProperties.VIDEO_NAME) ?
                properties.get(SystemProperties.VIDEO_NAME) : DEFAULT_VIDEO_NAME;
    }

    public ImageType getImageFileType() {
        return isPropertyExists(SystemProperties.IMAGE_FILE_TYPE) ?
                ImageType.valueOf(properties.get(SystemProperties.IMAGE_FILE_TYPE)) : DEFAULT_IMAGE_FILE_TYPE;
    }

    public boolean isScreenshotEnabled() {
        return isPropertyExists(SystemProperties.SCREENSHOTS_ENABLED) ?
                Boolean.parseBoolean(properties.get(SystemProperties.SCREENSHOTS_ENABLED)) : DEFAULT_SCREENSHOTS_ENABLED;
    }
    
    public boolean isVideoEnabled() {
        return isPropertyExists(SystemProperties.VIDEO_ENABLED) ?
                Boolean.parseBoolean(properties.get(SystemProperties.VIDEO_ENABLED)) : DEFAULT_VIDEO_ENABLED;
    }
    
    public RecordingType getRecordingType() {
        return isPropertyExists(SystemProperties.RECORDING_TYPE) ?
                RecordingType.valueOf(properties.get(SystemProperties.RECORDING_TYPE)) : DEFAULT_RECORDING_TYPE;
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
