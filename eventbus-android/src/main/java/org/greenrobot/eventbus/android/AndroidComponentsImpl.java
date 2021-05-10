package org.greenrobot.eventbus.android;

public class AndroidComponentsImpl extends AndroidComponents {

    public AndroidComponentsImpl() {
        super(new AndroidLogger("EventBus"), new DefaultAndroidMainThreadSupport());
    }
}
