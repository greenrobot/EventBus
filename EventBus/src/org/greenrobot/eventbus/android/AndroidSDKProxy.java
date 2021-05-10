package org.greenrobot.eventbus.android;

import org.greenrobot.eventbus.MainThreadSupport;

public abstract class AndroidSDKProxy {

    private static final AndroidSDKProxy implementation;

    static {
        implementation = AndroidDependenciesDetector.isAndroidSDKAvailable()
            ? AndroidDependenciesDetector.instantiateAndroidSDKProxy()
            : null;
    }

    public static boolean isAvailable() {
        return implementation != null;
    }

    public static AndroidSDKProxy get() {
        return implementation;
    }

    public final LogProxy log;
    public final MainThreadSupport defaultMainThreadSupport;

    public AndroidSDKProxy(LogProxy log, MainThreadSupport defaultMainThreadSupport) {
        this.log = log;
        this.defaultMainThreadSupport = defaultMainThreadSupport;
    }

    interface LogProxy {

        void println(int priority, String tag, String msg);

        int getVerboseLevelId();
        int getDebugLevelId();
        int getInfoLevelId();
        int getWarnLevelId();
        int getErrorLevelId();
    }
}
