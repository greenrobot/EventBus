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

/**
 * Provides a platform neutral logging target for all EVentBus logging..
 */
public final class EBLog {

    private static GenericLog log;

    public static void setLogTarget(GenericLog logTarget) {
        log = logTarget;
    }

    public static void v(String msg) {
        log.v(msg);
    }

    public static void v(String msg, Throwable tr) {
        log.v(msg, tr);
    }

    public static void d(String msg) {
        log.d(msg);
    }

    public static void d(String msg, Throwable tr) {
        log.d(msg, tr);
    }

    public static void i(String msg) {
        log.i(msg);
    }

    public static void i(String msg, Throwable tr) {
        log.i(msg, tr);
    }

    public static void w(String msg) {
        log.w(msg);
    }

    public static void w(String msg, Throwable tr) {
        log.w(msg, tr);
    }

    public static void w(Throwable tr) {
        log.w(tr);
    }

    public static void e(String msg) {
        log.e(msg);
    }

    public static void e(String msg, Throwable tr) {
        log.e(msg, tr);
    }

    public static void wtf(String msg) {
        log.wtf(msg);
    }

    public static void wtf(Throwable tr) {
        log.wtf(tr);
    }

    public static void wtf(String msg, Throwable tr) {
        log.wtf(msg, tr);
    }
}
