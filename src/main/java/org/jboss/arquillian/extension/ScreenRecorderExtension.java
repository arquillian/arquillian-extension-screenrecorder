/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.arquillian.extension;


import org.jboss.arquillian.core.spi.LoadableExtension;
/**
 *
 * @author pmensik
 */
public class ScreenRecorderExtension implements LoadableExtension {

    public void register(ExtensionBuilder builder) {
        builder.observer(LifecycleObserver.class);
    }
    
}
