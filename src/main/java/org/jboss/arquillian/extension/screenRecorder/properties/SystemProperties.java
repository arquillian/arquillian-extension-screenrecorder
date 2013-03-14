/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.arquillian.extension.screenRecorder.properties;

/**
 *
 * @author pmensik
 */
public final class SystemProperties {

    public static final String SCREEN_RECORDER = "screenRecorder"; 
    
    public static final String VIDEO_FOLDER = "videoFolder";
    public static final String SCREENSHOT_FOLDER = "screenshotFolder";
    public static final String MEDIA_ROOT = "rootFolder";
    public static final String VIDEO_NAME = "videoName";
    public static final String IMAGE_FILE_TYPE = "imageFileType";
    public static final String SCREENSHOTS_ENABLED = "screenshotsEnabled";
    public static final String VIDEO_ENABLED = "videoEnabled";
    public static final String RECORDING_TYPE = "recordingType";
    public static final String FRAME_RATE = "frameRate";
    public static final String TEST_TIMEOUT = "testTimeout";
    
    private SystemProperties() {
    }
    
}
