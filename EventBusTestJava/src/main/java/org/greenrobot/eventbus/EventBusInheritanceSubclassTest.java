package org.greenrobot.eventbus;

// Need to use upper class or Android test runner does not pick it up
public class EventBusInheritanceSubclassTest extends EventBusInheritanceTest {
    int countMyEventOverwritten;

    @Subscribe
    public void onEvent(MyEvent event) {
        countMyEventOverwritten++;
    }

}
