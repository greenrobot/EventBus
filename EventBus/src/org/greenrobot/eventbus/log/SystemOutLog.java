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
 * Logs to SystemOut.
 *
 * @author William Ferguson
 */
public final class SystemOutLog implements GenericLog {

    @Override
    public void v(String msg) {
        System.out.println("VERBOSE: " + msg);
    }

    @Override
    public void v(String msg, Throwable tr) {
        System.out.println("VERBOSE: " + msg);
        System.out.println("VERBOSE: " + tr.getMessage());
        tr.printStackTrace(System.out);
    }

    @Override
    public void d(String msg) {
        System.out.println("DEBUG: " + msg);
    }

    @Override
    public void d(String msg, Throwable tr) {
        System.out.println("DEBUG: " + msg);
        System.out.println("DEBUG: " + tr.getMessage());
        tr.printStackTrace(System.out);
    }

    @Override
    public void i(String msg) {
        System.out.println("INFO: " + msg);
    }

    @Override
    public void i(String msg, Throwable tr) {
        System.out.println("INFO: " + msg);
        System.out.println("INFO: " + tr.getMessage());
        tr.printStackTrace(System.out);
    }

    @Override
    public void w(String msg) {
        System.out.println("WARN: " + msg);
    }

    @Override
    public void w(String msg, Throwable tr) {
        System.out.println("WARN: " + msg);
        System.out.println("WARN: " + tr.getMessage());
        tr.printStackTrace(System.out);
    }

    @Override
    public void w(Throwable tr) {
        System.out.println("WARN: " + tr.getMessage());
        tr.printStackTrace(System.out);
    }

    @Override
    public void e(String msg) {
        System.out.println("ERROR: " + msg);
    }

    @Override
    public void e(String msg, Throwable tr) {
        System.out.println("ERROR: " + msg);
        System.out.println("ERROR: " + tr.getMessage());
        tr.printStackTrace(System.out);
    }

    @Override
    public void wtf(String msg) {
        System.out.println("WTF: " + msg);
    }

    @Override
    public void wtf(Throwable tr) {
        System.out.println("WTF: " + tr.getMessage());
        tr.printStackTrace(System.out);
    }

    @Override
    public void wtf(String msg, Throwable tr) {
        System.out.println("WTF: " + msg);
        System.out.println("WTF: " + tr.getMessage());
        tr.printStackTrace(System.out);
    }
}
