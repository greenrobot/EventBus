package de.greenrobot.event;

import android.util.Log;

import java.util.logging.Level;

public abstract class Logger {
    private static final boolean useAndroidLog;
    private static volatile Logger DEFAULT_LOGGER;


    static {
        boolean android = false;
        try {
            android = Class.forName("android.util.Log") != null;
        } catch (ClassNotFoundException e) {
            // OK
        }
        useAndroidLog = android;
    }


    public static synchronized Logger initDefaultLogger(String tag) {
        if (DEFAULT_LOGGER != null) {
            throw new IllegalStateException("Default logger already set up");
        }
        DEFAULT_LOGGER = create(tag);
        return DEFAULT_LOGGER;
    }

    public static Logger get() {
        if (DEFAULT_LOGGER == null) {
            throw new IllegalStateException("Default logger must be initialized before");
        }
        return DEFAULT_LOGGER;
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
            if (level == Level.OFF) {
                return 0;
            } else if (level == Level.FINEST || level == Level.FINER) {
                return Log.VERBOSE;
            } else if (level == Level.FINE || level == Level.CONFIG) {
                return Log.DEBUG;
            } else if (level == Level.INFO) {
                return Log.INFO;
            } else if (level == Level.WARNING) {
                return Log.WARN;
            } else if (level == Level.SEVERE) {
                return Log.ERROR;
            } else if (level == Level.ALL) {
                // Hmm, well..
                return Log.ASSERT;
            } else {
                throw new IllegalArgumentException("Unexpected level: " + level);
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
