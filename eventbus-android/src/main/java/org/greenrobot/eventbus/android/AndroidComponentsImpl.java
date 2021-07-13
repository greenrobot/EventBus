package org.greenrobot.eventbus.android;

/**
 * Used via reflection in the Java library by {@link AndroidDependenciesDetector}.
 */
public class AndroidComponentsImpl extends AndroidComponents {

    public AndroidComponentsImpl() {
        super(new AndroidLogger("EventBus"), new DefaultAndroidMainThreadSupport());
    }
}
