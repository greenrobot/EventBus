package de.greenrobot.event;

import android.annotation.TargetApi;
import android.os.Build;
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


    public abstract boolean isLoggable(int level);

    public abstract void v(String msg);

    public abstract void v(String msg, Throwable th);

    public abstract void d(String msg);

    public abstract void d(String msg, Throwable th);

    public abstract void i(String msg);

    public abstract void i(String msg, Throwable th);

    public abstract void w(String msg);

    public abstract void w(String msg, Throwable th);

    public abstract void w(Throwable th);

    public abstract void e(String msg);

    public abstract void e(String msg, Throwable th);

    public abstract void wtf(String msg);

    public abstract void wtf(String msg, Throwable th);

    public static class AndroidLogger extends Logger {
        public static final int VERBOSE = 2;
        public static final int DEBUG = 3;
        public static final int INFO = 4;
        public static final int WARN = 5;
        public static final int ERROR = 6;
        public static final int ASSERT = 7;

        private final String tag;

        public AndroidLogger(String tag) {
            this.tag = tag;
        }

        public boolean isLoggable(int level) {
            return Log.isLoggable(tag, level);
        }

        public void v(String msg) {
            Log.v(tag, msg);
        }

        public void v(String msg, Throwable th) {
            Log.v(tag, msg, th);
        }

        public void d(String msg) {
            Log.d(tag, msg);
        }

        public void d(String msg, Throwable th) {
            Log.d(tag, msg, th);
        }

        public void i(String msg) {
            Log.i(tag, msg);
        }

        public void i(String msg, Throwable th) {
            Log.i(tag, msg, th);
        }

        public void w(String msg) {
            Log.w(tag, msg);
        }

        public void w(String msg, Throwable th) {
            Log.w(tag, msg, th);
        }

        public void w(Throwable th) {
            Log.w(tag, th);
        }

        public void e(String msg) {
            Log.e(tag, msg);
        }

        public void e(String msg, Throwable th) {
            Log.e(tag, msg, th);
        }

        @TargetApi(Build.VERSION_CODES.FROYO)
        @Override
        public void wtf(String msg) {
            Log.wtf(tag, msg);
        }

        @TargetApi(Build.VERSION_CODES.FROYO)
        @Override
        public void wtf(String msg, Throwable th) {
            Log.wtf(tag, msg, th);
        }
    }

    public static class JavaLogger extends Logger {
        private static final Level[] LEVEL_MAP = {
                Level.OFF, Level.OFF, Level.OFF, // Unused
                Level.FINEST, // VERBOSE = 2
                Level.FINE, //DEBUG = 3
                Level.INFO, // INFO = 4
                Level.WARNING, // WARN = 5
                Level.SEVERE, //ERROR = 6
                Level.SEVERE, //ASSERT = 7
        };

        java.util.logging.Logger logger = java.util.logging.Logger.getLogger("");

        public JavaLogger(String tag) {
            logger = java.util.logging.Logger.getLogger(tag);
        }

        @Override
        public boolean isLoggable(int level) {
            return logger.isLoggable(LEVEL_MAP[level]);
        }

        @Override
        public void v(String msg) {
            logger.finest(msg);
        }

        @Override
        public void v(String msg, Throwable th) {
            logger.log(Level.FINEST, msg, th);
        }

        @Override
        public void d(String msg) {
            logger.fine(msg);
        }

        @Override
        public void d(String msg, Throwable th) {
            logger.log(Level.FINE, msg, th);
        }

        @Override
        public void i(String msg) {
            logger.info(msg);
        }

        @Override
        public void i(String msg, Throwable th) {
            logger.log(Level.INFO, msg, th);
        }

        @Override
        public void w(String msg) {
            logger.warning(msg);
        }

        @Override
        public void w(String msg, Throwable th) {
            logger.log(Level.WARNING, msg, th);
        }

        @Override
        public void w(Throwable th) {
            logger.log(Level.WARNING, null, th);
        }

        @Override
        public void e(String msg) {
            logger.severe(msg);
        }

        @Override
        public void e(String msg, Throwable th) {
            logger.log(Level.SEVERE, msg, th);
        }

        @Override
        public void wtf(String msg) {
            logger.severe(msg);
        }

        @Override
        public void wtf(String msg, Throwable th) {
            logger.log(Level.SEVERE, msg, th);
        }
    }
}
