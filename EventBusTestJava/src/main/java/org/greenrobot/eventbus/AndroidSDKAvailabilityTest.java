package org.greenrobot.eventbus;

import org.greenrobot.eventbus.android.AndroidSDKProxy;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class AndroidSDKAvailabilityTest {

    @Test
    public void shouldNotBeAvailable() {
        assertFalse(AndroidSDKProxy.isAvailable());
        assertNull(AndroidSDKProxy.get());
    }
}
