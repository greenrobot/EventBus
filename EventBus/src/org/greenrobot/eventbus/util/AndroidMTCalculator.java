package org.greenrobot.eventbus.util;

import android.os.Looper;

/**
 * Returns true if the current Thread is the Android Main Looper Thread.
 *
 * @author William Ferguson
 */
public class AndroidMTCalculator implements MainThreadCalculator {

    @Override
    public boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }
}
