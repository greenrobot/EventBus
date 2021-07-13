package org.greenrobot.eventbus.android;

import org.greenrobot.eventbus.Logger;
import org.greenrobot.eventbus.MainThreadSupport;

public abstract class AndroidComponents {

    private static final AndroidComponents implementation;

    static {
        implementation = AndroidDependenciesDetector.isAndroidSDKAvailable()
            ? AndroidDependenciesDetector.instantiateAndroidComponents()
            : null;
    }

    public static boolean areAvailable() {
        return implementation != null;
    }

    public static AndroidComponents get() {
        return implementation;
    }

    public final Logger logger;
    public final MainThreadSupport defaultMainThreadSupport;

    public AndroidComponents(Logger logger, MainThreadSupport defaultMainThreadSupport) {
        this.logger = logger;
        this.defaultMainThreadSupport = defaultMainThreadSupport;
    }
}
