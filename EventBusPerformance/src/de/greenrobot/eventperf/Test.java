package de.greenrobot.eventperf;

import java.util.concurrent.atomic.AtomicLong;

import android.content.Context;

public abstract class Test {
    protected final Context context;
    protected final TestParams params;
    protected AtomicLong eventsReceivedCount = new AtomicLong();
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
