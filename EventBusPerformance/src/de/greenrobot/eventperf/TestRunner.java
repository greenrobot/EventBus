package de.greenrobot.eventperf;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import de.greenrobot.event.EventBus;

/**
 * This thread initialize all selected tests and runs them through. Also the thread skips the tests, when it is canceled
 */
public class TestRunner extends Thread {
    private List<Test> tests;
    private volatile boolean canceled;
    private final EventBus controlBus;

    public TestRunner(Context context, TestParams testParams, EventBus controlBus) {
        this.controlBus = controlBus;
        tests = new ArrayList<Test>();
        for (Class<? extends Test> testClazz : testParams.getTestClasses()) {
            try {
                Constructor<?>[] constructors = testClazz.getConstructors();
                Constructor<? extends Test> constructor = testClazz.getConstructor(Context.class, TestParams.class);
                Test test = constructor.newInstance(context, testParams);
                tests.add(test);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void run() {

        int idx = 0;
        for (Test test : tests) {
            // Clean up and let the main thread calm down
            System.gc();
            try {
                Thread.sleep(300);
                System.gc();
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }

            test.prepareTest();
            if (!canceled) {
                test.runTest();
            }
            if (!canceled) {
                boolean isLastEvent = idx == tests.size() - 1;
                controlBus.post(new TestFinishedEvent(test, isLastEvent));
            }
            idx++;
        }

    }

    public List<Test> getTests() {
        return tests;
    }

    public void cancel() {
        canceled = true;
        for (Test test : tests) {
            test.cancel();
        }
    }
}
