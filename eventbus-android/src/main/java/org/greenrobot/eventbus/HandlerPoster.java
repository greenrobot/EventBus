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

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

public class HandlerPoster implements Poster {

    private Handler handler;
    private final PendingPostQueue queue;
    private final int maxMillisInsideHandleMessage;
    private final EventBus eventBus;
    private boolean handlerActive;

    public HandlerPoster(EventBus eventBus, Looper looper, int maxMillisInsideHandleMessage) {
        this.eventBus = eventBus;
        this.maxMillisInsideHandleMessage = maxMillisInsideHandleMessage;
        queue = new PendingPostQueue();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            handler = Handler.createAsync(looper, new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    return HandleMessageInHandlerPoster();
                }
            });
        } else {
            handler = new Handler(looper, new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    return HandleMessageInHandlerPoster();
                }
            });
        }
    }

    public void enqueue(Subscription subscription, Object event) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        synchronized (this) {
            queue.enqueue(pendingPost);
            if (!handlerActive) {
                handlerActive = true;
                if (!handler.sendMessage(handler.obtainMessage())) {
                    throw new EventBusException("Could not send handler message");
                }
            }
        }
    }

    private boolean HandleMessageInHandlerPoster() {
        boolean rescheduled = false;
        try {
            long started = SystemClock.uptimeMillis();
            while (true) {
                PendingPost pendingPost = queue.poll();
                if (pendingPost == null) {
                    synchronized (this) {
                        // Check again, this time in synchronized
                        pendingPost = queue.poll();
                        if (pendingPost == null) {
                            handlerActive = false;
                            return true;
                        }
                    }
                }
                eventBus.invokeSubscriber(pendingPost);
                long timeInMethod = SystemClock.uptimeMillis() - started;
                if (timeInMethod >= maxMillisInsideHandleMessage) {
                    if (!handler.sendMessage(handler.obtainMessage())) {
                        throw new EventBusException("Could not send handler message");
                    }
                    rescheduled = true;
                    return true;
                }
            }
        } finally {
            handlerActive = rescheduled;
        }
    }

}