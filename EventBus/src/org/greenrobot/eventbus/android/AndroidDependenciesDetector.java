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

    private static final String ANDROID_COMPONENTS_IMPLEMENTATION_CLASS_NAME = "org.greenrobot.eventbus.android.AndroidComponentsImpl";

    public static boolean areAndroidComponentsAvailable() {

        try {
            Class.forName(ANDROID_COMPONENTS_IMPLEMENTATION_CLASS_NAME);
            return true;
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public static AndroidComponents instantiateAndroidComponents() {

        try {
            Class<?> impl = Class.forName(ANDROID_COMPONENTS_IMPLEMENTATION_CLASS_NAME);
            return (AndroidComponents) impl.getConstructor().newInstance();
        }
        catch (Throwable ex) {
            return null;
        }
    }
}
