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
 * Means of logging regardless of what platform you are on.
 *
 * @author William Ferguson
 */
public interface GenericLog {

    void v(String msg);
    void v(String msg, Throwable tr);

    void d(String msg);
    void d(String msg, Throwable tr);

    void i(String msg);
    void i(String msg, Throwable tr);

    void w(String msg);
    void w(String msg, Throwable tr);
    void w(Throwable tr);

    void e(String msg);
    void e(String msg, Throwable tr);

    void wtf(String msg);
    void wtf(Throwable tr);
    void wtf(String msg, Throwable tr);
}
