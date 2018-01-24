package org.greenrobot.eventbus;

import org.junit.Ignore;

// Need to use upper class or Android test runner does not pick it up
public class EventBusInheritanceDisabledSubclassTest extends EventBusInheritanceDisabledTest {

    int countMyEventOverwritten;

    @Subscribe
    public void onEvent(MyEvent event) {
        countMyEventOverwritten++;
    }

    @Override
    @Ignore
    public void testEventClassHierarchy() {
        // TODO fix test in super, then remove this
    }
}