package org.greenrobot.eventbus;

import org.greenrobot.eventbus.android.AndroidComponents;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class AndroidComponentsAvailabilityOnJavaTest
{

    @Test
    public void shouldNotBeAvailable() {
        assertFalse(AndroidComponents.areAvailable());
        assertNull(AndroidComponents.get());
    }
}
