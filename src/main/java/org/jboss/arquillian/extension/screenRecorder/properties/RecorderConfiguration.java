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
    private static final String DEFAULT_IMAGE_FILE_TYPE = ImageType.PNG.name();
    private static final String DEFAULT_VIDEO_FILE_TYPE = "mp4";
    private static final String DEFAULT_FRAME_RATE = "20";
    private static final String DEFAULT_TEST_TIMEOUT = "3600"; //one hour
    private static final String DEFAULT_VIDEO = RecordingType.FAILURE.name();
    private static final String DEFAULT_SCREENSHOT = RecordingType.FAILURE.name();

    private Map<String, String> properties;

    public File getVideoFolder() {
        return FileUtils.getFile(getRootFolder(), getProperty(SystemProperties.VIDEO_FOLDER, DEFAULT_VIDEO_FOLDER));
    }

    public File getScreenshotFolder() {
        return FileUtils.getFile(getRootFolder(), getProperty(SystemProperties.SCREENSHOT_FOLDER, DEFAULT_SCREENSHOT_FOLDER));
    }

    public File getRootFolder() {
        return new File(getProperty(SystemProperties.ROOT_FOLDER, DEFAULT_ROOT_FOLDER));
    }

    public String getVideoName() {
        return getProperty(SystemProperties.VIDEO_NAME, DEFAULT_VIDEO_NAME);
    }

    public ImageType getImageFileType() {
        return ImageType.valueOf(getProperty(SystemProperties.IMAGE_FILE_TYPE, DEFAULT_IMAGE_FILE_TYPE).toUpperCase());
    }

    public int getFrameRate() {
        return Integer.parseInt(getProperty(SystemProperties.FRAME_RATE, DEFAULT_FRAME_RATE));
    }

    public long getTestTimeout() {
        return Long.parseLong(getProperty(SystemProperties.TEST_TIMEOUT, DEFAULT_TEST_TIMEOUT));
    }

    public RecordingType getVideoRecordingType() {
        return RecordingType.valueOf(getProperty(SystemProperties.VIDEO, DEFAULT_VIDEO).toUpperCase());
    }

    public RecordingType getScreenshotRecordingType() {
        return RecordingType.valueOf(getProperty(SystemProperties.SCREENSHOT, DEFAULT_SCREENSHOT).toUpperCase());
    }

    public String getVideoFileType() {
        return DEFAULT_VIDEO_FILE_TYPE;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    protected String getProperty(String name, String defaultValue) {
        if (name == null) {
            throw new IllegalArgumentException("name");
        }
        if (properties == null) {
            return defaultValue;
        }
        String found = properties.get(name);
        if (found == null || found.isEmpty()) {
            return defaultValue;
        } else {
            return found;
        }
    }

}
