package de.greenrobot.eventperf;

import java.io.Serializable;
import java.util.ArrayList;

import de.greenrobot.event.ThreadMode;

public class TestParams implements Serializable {
    private static final long serialVersionUID = -2739435088947740809L;

    private int eventCount;
    private int subscriberCount;
    private int publisherCount;
    private ThreadMode threadMode;
    private boolean eventInheritance;
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
