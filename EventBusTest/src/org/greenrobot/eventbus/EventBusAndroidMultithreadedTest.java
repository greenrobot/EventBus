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

import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

@RunWith(AndroidJUnit4.class)
public class EventBusAndroidMultithreadedTest extends EventBusMultithreadedTest {

    @Test
    public void testSubscribeUnSubscribeAndPostMixedEventType() throws InterruptedException {
        List<SubscribeUnsubscribeThread> threads = new ArrayList<SubscribeUnsubscribeThread>();

        // Debug.startMethodTracing("testSubscribeUnSubscribeAndPostMixedEventType");
        for (int i = 0; i < 5; i++) {
            SubscribeUnsubscribeThread thread = new SubscribeUnsubscribeThread();
            thread.start();
            threads.add(thread);
        }
        // This test takes a bit longer, so just use fraction the regular count
        runThreadsMixedEventType(COUNT / 4, 5);
        for (SubscribeUnsubscribeThread thread : threads) {
            thread.shutdown();
        }
        for (SubscribeUnsubscribeThread thread : threads) {
            thread.join();
        }
        // Debug.stopMethodTracing();
    }

    public class SubscribeUnsubscribeThread extends Thread {
        boolean running = true;

        public void shutdown() {
            running = false;
        }

        @Override
        public void run() {
            try {
                while (running) {
                    eventBus.register(this);
                    double random = Math.random();
                    if (random > 0.6d) {
                        Thread.sleep(0, (int) (1000000 * Math.random()));
                    } else if (random > 0.3d) {
                        Thread.yield();
                    }
                    eventBus.unregister(this);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(String event) {
            assertSame(Looper.getMainLooper(), Looper.myLooper());
        }

        @Subscribe(threadMode = ThreadMode.BACKGROUND)
        public void onEventBackgroundThread(Integer event) {
            assertNotSame(Looper.getMainLooper(), Looper.myLooper());
        }

        @Subscribe
        public void onEvent(Object event) {
            assertNotSame(Looper.getMainLooper(), Looper.myLooper());
        }

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void onEventAsync(Object event) {
            assertNotSame(Looper.getMainLooper(), Looper.myLooper());
        }
    }

}
