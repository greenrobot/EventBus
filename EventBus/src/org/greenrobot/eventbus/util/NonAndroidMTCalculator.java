package org.greenrobot.eventbus.util;

/**
 * Always returns false as the Main Looper Thread only occurs within Android.
 */
public class NonAndroidMTCalculator implements MainThreadCalculator {

    @Override
    public boolean isMainThread() {
        return false;
    }
}
