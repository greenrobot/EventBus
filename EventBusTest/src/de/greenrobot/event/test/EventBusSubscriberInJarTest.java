package de.greenrobot.event.test;

import de.greenrobot.event.EventBus;
import junit.framework.Assert;
import junit.framework.TestCase;

public class EventBusSubscriberInJarTest extends TestCase {
    public void testSubscriberInJar() {
        SubscriberInJar subscriber = new SubscriberInJar();
        EventBus eventBus = EventBus.builder().build();
        eventBus.register(subscriber);
        eventBus.post("Hi Jar");
        eventBus.post(42);
        Assert.assertEquals(1, subscriber.getCollectedStrings().size());
        Assert.assertEquals("Hi Jar", subscriber.getCollectedStrings().get(0));
    }
}
