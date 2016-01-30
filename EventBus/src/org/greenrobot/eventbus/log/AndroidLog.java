/*
 * Copyright (c) Xandar IP 2013.
 *
 * All Rights Reserved
 * No part of this application may be reproduced, copied, modified or adapted, without the prior written consent
 * of the author, unless otherwise indicated for stand-alone materials.
 *
 * Contact support@xandar.com.au for copyright requests.
 */

package org.greenrobot.eventbus.log;

import android.util.Log;

/**
 * Logs to the Android log.
 */
public final class AndroidLog implements GenericLog {

    private final String tag;

    public AndroidLog(String tag) {
        this.tag = tag;
    }

    @Override
    public void v(String msg) {
        Log.v(tag, msg);
    }

    @Override
    public void v(String msg, Throwable tr) {
        Log.v(tag, msg, tr);
    }

    @Override
    public void d(String msg) {
        Log.d(tag, msg);
    }

    @Override
    public void d(String msg, Throwable tr) {
        Log.d(tag, msg, tr);
    }

    @Override
    public void i(String msg) {
        Log.i(tag, msg);
    }

    @Override
    public void i(String msg, Throwable tr) {
        Log.i(tag, msg, tr);
    }

    @Override
    public void w(String msg) {
        Log.w(tag, msg);
    }

    @Override
    public void w(String msg, Throwable tr) {
        Log.w(tag, msg, tr);
    }

    @Override
    public void w(Throwable tr) {
        Log.w(tag, tr);
    }

    @Override
    public void e(String msg) {
        Log.e(tag, msg);
    }

    @Override
    public void e(String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }

    @Override
    public void wtf(String msg) {
        Log.wtf(tag, msg);
    }

    @Override
    public void wtf(Throwable tr) {
        Log.wtf(tag, tr);
    }

    @Override
    public void wtf(String msg, Throwable tr) {
        Log.wtf(tag, msg, tr);
    }
}
