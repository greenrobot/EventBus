package org.greenrobot.eventbus;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.greenrobot.eventbus.EventBus;
import org.junit.Test;

import de.greenrobot.event.test.SubscriberInJar;

public class EventBusSubscriberInJarTest extends TestCase {
    protected EventBus eventBus = EventBus.builder().build();

    @Test
    public void testSubscriberInJar() {
        SubscriberInJar subscriber = new SubscriberInJar();
        eventBus.register(subscriber);
        eventBus.post("Hi Jar");
        eventBus.post(42);
        Assert.assertEquals(1, subscriber.getCollectedStrings().size());
        Assert.assertEquals("Hi Jar", subscriber.getCollectedStrings().get(0));
    }
}
