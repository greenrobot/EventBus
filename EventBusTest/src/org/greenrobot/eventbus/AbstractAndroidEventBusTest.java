/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.greenrobot.eventbus;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.runner.RunWith;


import static org.junit.Assert.assertFalse;

/**
 * @author Markus Junginger, greenrobot
 */
@RunWith(AndroidJUnit4.class)
public abstract class AbstractAndroidEventBusTest extends AbstractEventBusTest {
    private EventPostHandler mainPoster;

    public AbstractAndroidEventBusTest() {
        this(false);
    }

    public AbstractAndroidEventBusTest(boolean collectEventsReceived) {
        super(collectEventsReceived);
    }

    @Before
    public void setUpAndroid() throws Exception {
        mainPoster = new EventPostHandler(Looper.getMainLooper());
        assertFalse(Looper.getMainLooper().getThread().equals(Thread.currentThread()));
    }

    protected void postInMainThread(Object event) {
        mainPoster.post(event);
    }

    @SuppressLint("HandlerLeak")
    class EventPostHandler extends Handler {
        public EventPostHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            eventBus.post(msg.obj);
        }

        void post(Object event) {
            sendMessage(obtainMessage(0, event));
        }

    }

}
