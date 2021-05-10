package org.greenrobot.eventbus.android;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("TryWithIdenticalCatches")
public class AndroidDependenciesDetector {

    public static boolean isAndroidSDKAvailable() {

        try {
            Class<?> looperClass = Class.forName("android.os.Looper");
            Method getMainLooper = looperClass.getDeclaredMethod("getMainLooper");
            Object mainLooper = getMainLooper.invoke(null);
            return mainLooper != null;
        }
        catch (ClassNotFoundException ignored) {}
        catch (NoSuchMethodException ignored) {}
        catch (IllegalAccessException ignored) {}
        catch (InvocationTargetException ignored) {}

        return false;
    }

    public static boolean isAndroidSDKProxyImplAvailable() {

        try {
            Class.forName("org.greenrobot.eventbus.android.AndroidSDKProxyImpl");
            return true;
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public static AndroidSDKProxy instantiateAndroidSDKProxy() {

        try {
            Class<?> impl = Class.forName("org.greenrobot.eventbus.android.AndroidSDKProxyImpl");
            return (AndroidSDKProxy) impl.getConstructor().newInstance();
        }
        catch (Throwable ex) {
            return null;
        }
    }
}
