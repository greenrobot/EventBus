/*
 * Copyright (C) 2012 Markus Junginger, greenrobot (http://greenrobot.de)
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
package de.greenrobot.event;

import java.lang.reflect.Method;

final class SubscriberMethod {
    final Method method;
    final ThreadMode threadMode;
    final Class<?> eventType;

    SubscriberMethod(Method method, ThreadMode threadMode, Class<?> eventType) {
        this.method = method;
        this.threadMode = threadMode;
        this.eventType = eventType;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SubscriberMethod) {
            SubscriberMethod otherSubscription = (SubscriberMethod) other;
            // Super slow (improve once used): http://code.google.com/p/android/issues/detail?id=7811
            return method.equals(otherSubscription.method);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        // Check performance once used
        return method.hashCode();
    }
}