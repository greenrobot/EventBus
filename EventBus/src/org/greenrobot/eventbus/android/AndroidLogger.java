/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.greenrobot.eventbus.android;

import android.util.Log;

import org.greenrobot.eventbus.Logger;

import java.util.logging.Level;

public class AndroidLogger implements Logger {

    private static final boolean ANDROID_LOG_AVAILABLE;

    static {
        boolean android = false;
        try {
            android = Class.forName("android.util.Log") != null;
        } catch (ClassNotFoundException e) {
            // OK
        }
        ANDROID_LOG_AVAILABLE = android;
    }

    public static boolean isAndroidLogAvailable() {
        return ANDROID_LOG_AVAILABLE;
    }


    private final String tag;

    public AndroidLogger(String tag) {
        this.tag = tag;
    }

    public void log(Level level, String msg) {
        if (level != Level.OFF) {
            Log.println(mapLevel(level), tag, msg);
        }
    }

    public void log(Level level, String msg, Throwable th) {
        if (level != Level.OFF) {
            // That's how Log does it internally
            Log.println(mapLevel(level), tag, msg + "\n" + Log.getStackTraceString(th));
        }
    }

    private int mapLevel(Level level) {
        int value = level.intValue();
        if (value < 800) { // below INFO
            if (value < 500) { // below FINE
                return Log.VERBOSE;
            } else {
                return Log.DEBUG;
            }
        } else if (value < 900) { // below WARNING
            return Log.INFO;
        } else if (value < 1000) { // below ERROR
            return Log.WARN;
        } else {
            return Log.ERROR;
        }
    }
}
