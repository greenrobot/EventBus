package de.greenrobot.event.util;

import android.util.Log;
import de.greenrobot.event.CustomLogger;

/**
 * Default simple implementation passes logs to {@code android.util.Log}.
 */
public class SimpleLogger implements CustomLogger {

    @Override
    public void v(String tag, String text, Throwable t) {
        Log.v(tag, text, t);
    }

    @Override
    public void d(String tag, String text, Throwable t) {
        Log.d(tag, text, t);
    }

    @Override
    public void i(String tag, String text, Throwable t) {
        Log.i(tag, text, t);
    }

    @Override
    public void w(String tag, String text, Throwable t) {
        Log.w(tag, text, t);
    }

    @Override
    public void e(String tag, String text, Throwable t) {
        Log.e(tag, text, t);
    }
}
