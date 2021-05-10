package org.greenrobot.eventbus.android;

public class AndroidSDKProxyImpl extends AndroidSDKProxy {

    public AndroidSDKProxyImpl() {
        super(new AndroidLogProxyImpl(), new DefaultAndroidMainThreadSupport());
    }
}
