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

package org.greenrobot.eventbusperf;

import android.content.Context;

import java.util.concurrent.atomic.AtomicLong;

public abstract class Test {
    protected final Context context;
    protected final TestParams params;
    public final AtomicLong eventsReceivedCount = new AtomicLong();
    protected long primaryResultMicros;
    protected int primaryResultCount;
    protected String otherTestResults;

    protected boolean canceled;

    public Test(Context context, TestParams params) {
        this.context = context;
        this.params = params;
    }

    public void cancel() {
        canceled = true;
    }

    /** prepares the test, all things which are not relevant for test results */
    public abstract void prepareTest();

    public abstract void runTest();

    /** returns the display name of the test. e.g. EventBus */
    public abstract String getDisplayName();

    protected void waitForReceivedEventCount(int expectedEventCount) {
        while (eventsReceivedCount.get() < expectedEventCount) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public long getPrimaryResultMicros() {
        return primaryResultMicros;
    }

    public double getPrimaryResultRate() {
        return primaryResultCount / (primaryResultMicros / 1000000d);
    }

    public String getOtherTestResults() {
        return otherTestResults;
    }

}
