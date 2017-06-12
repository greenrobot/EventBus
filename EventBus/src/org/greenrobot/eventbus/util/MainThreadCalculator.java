package org.greenrobot.eventbus.util;

/**
 * Determines whether the current Thread is the Android Main Looper Thread.
 *
 * @author William Ferguson
 */
public interface MainThreadCalculator {

    /**
     * @return true if the current Thread is the Android Main Looper Thread.
     */
    boolean isMainThread();
}
