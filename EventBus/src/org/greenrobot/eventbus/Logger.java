/*
 * Copyright (C) 2012-2020 Markus Junginger, greenrobot (http://greenrobot.org)
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
package org.greenrobot.eventbus;

import android.os.Looper;
import org.greenrobot.eventbus.android.AndroidLogger;

import java.util.logging.Level;

public interface Logger {

    void log(Level level, String msg);

    void log(Level level, String msg, Throwable th);

    class JavaLogger implements Logger {
        protected final java.util.logging.Logger logger;

        public JavaLogger(String tag) {
            logger = java.util.logging.Logger.getLogger(tag);
        }

        @Override
        public void log(Level level, String msg) {
            // TODO Replace logged method with caller method
            logger.log(level, msg);
        }

        @Override
        public void log(Level level, String msg, Throwable th) {
            // TODO Replace logged method with caller method
            logger.log(level, msg, th);
        }

    }

    class SystemOutLogger implements Logger {

        @Override
        public void log(Level level, String msg) {
            System.out.println("[" + level + "] " + msg);
        }

        @Override
        public void log(Level level, String msg, Throwable th) {
            System.out.println("[" + level + "] " + msg);
            th.printStackTrace(System.out);
        }

    }

    class Default {
        public static Logger get() {
            // also check main looper to see if we have "good" Android classes (not Stubs etc.)
            return AndroidLogger.isAndroidLogAvailable() && getAndroidMainLooperOrNull() != null
                    ? new AndroidLogger("EventBus") :
                    new Logger.SystemOutLogger();
        }

        static Object getAndroidMainLooperOrNull() {
            try {
                return Looper.getMainLooper();
            } catch (RuntimeException e) {
                // Not really a functional Android (e.g. "Stub!" maven dependencies)
                return null;
            }
        }
    }

}
