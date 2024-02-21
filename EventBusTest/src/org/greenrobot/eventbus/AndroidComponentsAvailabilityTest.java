package org.greenrobot.eventbus;

import org.greenrobot.eventbus.android.AndroidComponents;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AndroidComponentsAvailabilityTest {

    @Test
    public void shouldBeAvailable() {
        assertTrue(AndroidComponents.areAvailable());
        assertNotNull(AndroidComponents.get());
    }
}
