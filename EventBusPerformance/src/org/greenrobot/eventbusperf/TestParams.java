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

import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;

public class TestParams implements Serializable {
    private static final long serialVersionUID = -2739435088947740809L;

    private int eventCount;
    private int subscriberCount;
    private int publisherCount;
    private ThreadMode threadMode;
    private boolean eventInheritance;
    private boolean ignoreGeneratedIndex;
    private int testNumber;
    private ArrayList<Class<? extends Test>> testClasses;

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int iterations) {
        this.eventCount = iterations;
    }

    public int getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(int subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public int getPublisherCount() {
        return publisherCount;
    }

    public void setPublisherCount(int publisherCount) {
        this.publisherCount = publisherCount;
    }

    public ThreadMode getThreadMode() {
        return threadMode;
    }

    public void setThreadMode(ThreadMode threadMode) {
        this.threadMode = threadMode;
    }

    public boolean isEventInheritance() {
        return eventInheritance;
    }

    public void setEventInheritance(boolean eventInheritance) {
        this.eventInheritance = eventInheritance;
    }

    public boolean isIgnoreGeneratedIndex() {
        return ignoreGeneratedIndex;
    }

    public void setIgnoreGeneratedIndex(boolean ignoreGeneratedIndex) {
        this.ignoreGeneratedIndex = ignoreGeneratedIndex;
    }

    public ArrayList<Class<? extends Test>> getTestClasses() {
        return testClasses;
    }

    public void setTestClasses(ArrayList<Class<? extends Test>> testClasses) {
        this.testClasses = testClasses;
    }

    public int getTestNumber() {
        return testNumber;
    }

    public void setTestNumber(int testNumber) {
        this.testNumber = testNumber;
    }

}
