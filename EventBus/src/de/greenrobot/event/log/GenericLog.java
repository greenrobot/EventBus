/*
 * Copyright (c) Xandar IP 2013.
 *
 * All Rights Reserved
 * No part of this application may be reproduced, copied, modified or adapted, without the prior written consent
 * of the author, unless otherwise indicated for stand-alone materials.
 *
 * Contact support@xandar.com.au for copyright requests.
 */

package de.greenrobot.event.log;

/**
 * Means of logging regardless of what platform you are on.
 *
 * @author William Ferguson
 */
public interface GenericLog {

    public void v(String msg);
    public void v(String msg, Throwable tr);

    public void d(String msg);
    public void d(String msg, Throwable tr);

    public void i(String msg);
    public void i(String msg, Throwable tr);

    public void w(String msg);
    public void w(String msg, Throwable tr);
    public void w(Throwable tr);

    public void e(String msg);
    public void e(String msg, Throwable tr);

    public void wtf(String msg);
    public void wtf(Throwable tr);
    public void wtf(String msg, Throwable tr);
}
