/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.greenrobot.eventbus.meta;

import org.greenrobot.eventbus.EventBusException;
import org.greenrobot.eventbus.GeneratedSubscriberMethod;
import org.greenrobot.eventbus.ReflectiveSubscriberMethod;
import org.greenrobot.eventbus.SubscriberMethod;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Method;

/** Base class for generated subscriber meta info classes created by annotation processing. */
public abstract class AbstractSubscriberInfo implements SubscriberInfo {
    private final Class subscriberClass;
    private final Class<? extends SubscriberInfo> superSubscriberInfoClass;
    private final boolean shouldCheckSuperclass;

    protected AbstractSubscriberInfo(Class subscriberClass, Class<? extends SubscriberInfo> superSubscriberInfoClass,
                                     boolean shouldCheckSuperclass) {
        this.subscriberClass = subscriberClass;
        this.superSubscriberInfoClass = superSubscriberInfoClass;
        this.shouldCheckSuperclass = shouldCheckSuperclass;
    }

    @Override
    public Class getSubscriberClass() {
        return subscriberClass;
    }

    @Override
    public SubscriberInfo getSuperSubscriberInfo() {
        if(superSubscriberInfoClass == null) {
            return null;
        }
        try {
            return superSubscriberInfoClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean shouldCheckSuperclass() {
        return shouldCheckSuperclass;
    }


}
