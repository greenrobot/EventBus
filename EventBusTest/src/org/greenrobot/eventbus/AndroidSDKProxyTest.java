package org.greenrobot.eventbus;

import org.greenrobot.eventbus.android.AndroidSDKProxy;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AndroidSDKProxyTest {

    @Test
    public void shouldBeAvailable() {
        assertTrue(AndroidSDKProxy.isAvailable());
        assertNotNull(AndroidSDKProxy.get());
    }
}
