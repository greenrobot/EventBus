package de.greenrobot.event.test.indexed;

import de.greenrobot.event.EventBus;
import org.greenrobot.eventbus.EventBusTestsIndex;

public class Indexed {
    static EventBus build() {
        return EventBus.builder().addIndex(new EventBusTestsIndex()).build();
    }
}
