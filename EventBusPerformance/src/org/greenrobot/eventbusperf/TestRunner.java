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

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * This thread initialize all selected tests and runs them through. Also the thread skips the tests, when it is canceled
 */
public class TestRunner extends Thread {
    private final EventBus controlBus;
    private List<Test> mTests;
    private volatile boolean mCanceled;

    public TestRunner(Context context, TestParams testParams, EventBus controlBus) {
        this.controlBus = controlBus;
        mTests = new ArrayList<Test>();
        for (Class<? extends Test> testClazz : testParams.getTestClasses()) {
            try {
                Constructor<?>[] constructors = testClazz.getConstructors();
                Constructor<? extends Test> constructor = testClazz.getConstructor(Context.class, TestParams.class);
                Test test = constructor.newInstance(context, testParams);
                mTests.add(test);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void run() {
        int idx = 0;
        for (Test test : mTests) {
            // Clean up and let the main thread calm down
            System.gc();
            try {
                Thread.sleep(300);
                System.gc();
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }

            test.prepareTest();
            if (!mCanceled) {
                test.runTest();
            }
            if (!mCanceled) {
                boolean isLastEvent = idx == mTests.size() - 1;
                controlBus.post(new TestFinishedEvent(test, isLastEvent));
            }
            idx++;
        }
    }

    public List<Test> getTests() {
        return mTests;
    }

    public void cancel() {
        mCanceled = true;
        for (Test test : mTests) {
            test.cancel();
        }
    }
}