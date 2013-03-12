/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.arquillian.extension.screenRecorder;

/**
 *
 * @author pmensik
 */
public enum ImageType {
    
    PNG("png"), JPG("jpg"), GIF("gif");
    
    private ImageType(String imageType) {
        this.imageType = imageType;
    }
    
    private String imageType;

    @Override
    public String toString() {
        return imageType;
    }
    
}
