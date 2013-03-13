/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.arquillian.extension.screenRecorder;

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
    public static final String RECORD_TESTS_SEPARATELY = "recordEachTestSeparately";
    public static final String SHOULD_TAKE_SCREENSHOTS = "shouldTakeScreenshots";
    public static final String SHOULD_TAKE_SCREENSHOTS_ONLY_ON_FAIL = "screenshotsOnFail";
    public static final String SHOULD_RECORD_VIDEO = "shouldRecordVideo";
    public static final String FRAME_RATE = "frameRate";
    public static final String TEST_TIMEOUT = "testTimeout";
    
    private SystemProperties() {
    }
    
}
