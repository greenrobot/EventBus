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

package org.greenrobot.eventbusperf.testsubject;

import org.greenrobot.eventbus.Subscribe;

import org.greenrobot.eventbusperf.TestEvent;

public class SubscribeClassEventBusDefault {
    private PerfTestEventBus perfTestEventBus;

    public SubscribeClassEventBusDefault(PerfTestEventBus perfTestEventBus) {
        this.perfTestEventBus = perfTestEventBus;
    }

    @Subscribe
    public void onEvent(TestEvent event) {
        perfTestEventBus.eventsReceivedCount.incrementAndGet();
    }

    public void dummy() {
    }

    public void dummy2() {
    }

    public void dummy3() {
    }

    public void dummy4() {
    }

    public void dummy5() {
    }
}
