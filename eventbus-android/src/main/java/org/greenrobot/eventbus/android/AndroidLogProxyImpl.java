package org.greenrobot.eventbus.android;

import android.util.Log;

public class AndroidLogProxyImpl implements AndroidSDKProxy.LogProxy {

    @Override
    public void println(int priority, String tag, String msg) {
        Log.println(priority, tag, msg);
    }

    @Override
    public int getVerboseLevelId() {
        return Log.VERBOSE;
    }

    @Override
    public int getDebugLevelId() {
        return Log.DEBUG;
    }

    @Override
    public int getInfoLevelId() {
        return Log.INFO;
    }

    @Override
    public int getWarnLevelId() {
        return Log.WARN;
    }

    @Override
    public int getErrorLevelId() {
        return Log.ERROR;
    }
}
