package de.greenrobot.eventperf;

import java.io.Serializable;
import java.util.ArrayList;

import de.greenrobot.event.ThreadMode;

public class TestParams implements Serializable {
    private static final long serialVersionUID = -2739435088947740809L;

    private int iterations;
    private int subscriberCount;
    private int publisherCount;
    private ThreadMode threadMode;
    private ArrayList<Class<? extends Test>> testClasses;

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
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

    public ArrayList<Class<? extends Test>> getTestClasses() {
        return testClasses;
    }

    public void setTestClasses(ArrayList<Class<? extends Test>> testClasses) {
        this.testClasses = testClasses;
    }
}
