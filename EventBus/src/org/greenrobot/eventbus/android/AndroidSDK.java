package org.greenrobot.eventbus.android;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("TryWithIdenticalCatches")
public final class AndroidSDK
{

    private static final AndroidSDK implementation;

    static {
        boolean isAndroidSDKAvailable = false;

        try {
            Class<?> looperClass = Class.forName("android.os.Looper");
            Method getMainLooper = looperClass.getDeclaredMethod("getMainLooper");
            Object mainLooper = getMainLooper.invoke(null);
            isAndroidSDKAvailable = mainLooper != null;
        }
        catch (ClassNotFoundException ignored) {}
        catch (NoSuchMethodException ignored) {}
        catch (IllegalAccessException ignored) {}
        catch (InvocationTargetException ignored) {}

        implementation = isAndroidSDKAvailable ? new AndroidSDK() : null;
    }

    public static boolean isAvailable() {
        return implementation != null;
    }

    public static AndroidSDK get() {
        return implementation;
    }

    public final Log log = new Log();
    public static final class Log {

        //android.util.Log
        //public static  int println(int priority, java.lang.String tag, java.lang.String msg) { throw new RuntimeException("Stub!"); }
        public void println(int priority, String tag, String msg) {
        }

        public LogLevels getLogLevels() {
            return new LogLevels();
        }

        public static final class LogLevels {

            public int verbose = 0;
            public int debug = 0;
            public int info = 0;
            public int warn = 0;
            public int error = 0;
        }
    }

    public final Looper looper = new Looper();
    public static final class Looper {

        public static Looper myLooper() {
            return null;
        }
    }

    public static class Handler {

        public Handler(Looper looper) {

        }

        public Message obtainMessage() {
            return new Message();
        }

        public boolean sendMessage(Message message) {
            return true;
        }

        public void handleMessage(Message message) {

        }
    }

    public static class Message {

    }

    public final SystemClock systemClock = new SystemClock();
    public static class SystemClock {

        public long uptimeMillis() {
            return 0;
        }
    }
}
