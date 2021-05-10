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

import org.greenrobot.eventbus.Logger;
import org.greenrobot.eventbus.util.ExceptionStackTraceUtils;
import java.util.logging.Level;

public class AndroidLogger implements Logger {

    private final AndroidSDK androidSDK;
    private final String tag;

    public AndroidLogger(AndroidSDK androidSDK, String tag) {
        this.androidSDK = androidSDK;
        this.tag = tag;
    }

    public void log(Level level, String msg) {
        if (level != Level.OFF) {
            androidSDK.log.println(mapLevel(level), tag, msg);
        }
    }

    public void log(Level level, String msg, Throwable th) {
        if (level != Level.OFF) {
            // That's how Log does it internally
            androidSDK.log.println(mapLevel(level), tag, msg + "\n" + ExceptionStackTraceUtils.getStackTraceAsString(th));
        }
    }

    private int mapLevel(Level level) {
        int value = level.intValue();
        if (value < 800) { // below INFO
            if (value < 500) { // below FINE
                return androidSDK.log.getLogLevels().verbose;
            } else {
                return androidSDK.log.getLogLevels().debug;
            }
        } else if (value < 900) { // below WARNING
            return androidSDK.log.getLogLevels().info;
        } else if (value < 1000) { // below ERROR
            return androidSDK.log.getLogLevels().warn;
        } else {
            return androidSDK.log.getLogLevels().error;
        }
    }
}
