package de.greenrobot.event;

import android.util.Log;

import java.util.logging.Level;

public abstract class Logger {
    private static final boolean useAndroidLog;

    static {
        boolean android = false;
        try {
            android = Class.forName("android.util.Log") != null;
        } catch (ClassNotFoundException e) {
            // OK
        }
        useAndroidLog = android;
    }

    public static Logger create(String tag) {
        if (useAndroidLog) {
            return new AndroidLogger(tag);
        } else {
            return new JavaLogger(tag);
        }
    }

    public abstract boolean isLoggable(Level level);

    public abstract void log(Level level, String msg);

    public abstract void log(Level level, String msg, Throwable th);

    public static class AndroidLogger extends Logger {
        private final String tag;

        public AndroidLogger(String tag) {
            this.tag = tag;
        }

        public boolean isLoggable(Level level) {
            if (level == Level.OFF) {
                return false;
            } else {
                return Log.isLoggable(tag, mapLevel(level));
            }
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

        protected int mapLevel(Level level) {
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

    public static class JavaLogger extends Logger {
        protected final java.util.logging.Logger logger;

        public JavaLogger(String tag) {
            logger = java.util.logging.Logger.getLogger(tag);
        }

        @Override
        public boolean isLoggable(Level level) {
            return logger.isLoggable(level);
        }

        @Override
        public void log(Level level, String msg) {
            logger.log(level, msg);
        }

        @Override
        public void log(Level level, String msg, Throwable th) {
            logger.log(level, msg, th);
        }

    }
}
