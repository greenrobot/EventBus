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

import android.util.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class SubscriberMethodFinder {
    private static final String ON_EVENT_METHOD_NAME = "onEvent";

    /*
     * In newer class files, compilers may add methods. Those are called bridge
     * or synthetic methods. EventBus must ignore both. There modifiers are not
     * public but defined in the Java class file format:
     * http://docs.oracle.com/javase
     * /specs/jvms/se7/html/jvms-4.html#jvms-4.6-200-A.1
     */
    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;

    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE
            | SYNTHETIC;
    private static final Map<String, List<SubscriberMethod>> methodCache = new HashMap<String, List<SubscriberMethod>>();

    private final Map<Class<?>, Class<?>> skipMethodVerificationForClasses;

    SubscriberMethodFinder(List<Class<?>> skipMethodVerificationForClassesList) {
        skipMethodVerificationForClasses = new ConcurrentHashMap<Class<?>, Class<?>>();
        if (skipMethodVerificationForClassesList != null) {
            for (Class<?> clazz : skipMethodVerificationForClassesList) {
                skipMethodVerificationForClasses.put(clazz, clazz);
            }
        }
    }

    List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
        String key = subscriberClass.getName();
        List<SubscriberMethod> subscriberMethods;
        synchronized (methodCache) {
            subscriberMethods = methodCache.get(key);
        }
        if (subscriberMethods != null) {
            return subscriberMethods;
        }
        subscriberMethods = new ArrayList<SubscriberMethod>();
        Class<?> clazz = subscriberClass;
        while (clazz != null) {
            if (isSystemCalss(clazz.getName())) {
                // Skip system classes, this just degrades performance
                break;
            }

            // Starting with EventBus 2.2 we enforced methods to be public
            // (might change with annotations again)
            findAllOnEventMethods(clazz, subscriberMethods);
            clazz = clazz.getSuperclass();
        }
        if (subscriberMethods.isEmpty()) {
            throw new EventBusException("Subscriber " + subscriberClass
                    + " has no public methods called "
                    + ON_EVENT_METHOD_NAME);
        } else {
            synchronized (methodCache) {
                methodCache.put(key, subscriberMethods);
            }
            return subscriberMethods;
        }
    }

    private void findAllOnEventMethods(Class<?> clazz, List<SubscriberMethod> subscriberMethods) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (isTheMethodValid(method)) {
                String modifierString = methodName.substring(ON_EVENT_METHOD_NAME
                        .length());
                ThreadMode threadMode = parseThreadMode(modifierString);
                if (threadMode == null) {
                    if (skipMethodVerificationForClasses.containsKey(clazz)) {
                        continue;
                    } else {
                        throw new EventBusException(
                                "Illegal onEvent method, check for typos: " + method);
                    }
                }

                // construct a SubscriberMethod
                SubscriberMethod newSubscriberMethod = constructScriberMethod(method,
                        threadMode);
                if (newSubscriberMethod != null) {
                    subscriberMethods.add(newSubscriberMethod);
                }
            } else if (!skipMethodVerificationForClasses.containsKey(clazz)) {
                Log.d(EventBus.TAG, "Skipping method (not public, static or abstract): "
                        + clazz + "."
                        + methodName);
            }
        }
    }

    private ThreadMode parseThreadMode(String modifierString) {
        ThreadMode threadMode = null;
        if (modifierString.length() == 0) {
            threadMode = ThreadMode.PostThread;
        } else if (modifierString.equals("MainThread")) {
            threadMode = ThreadMode.MainThread;
        } else if (modifierString.equals("BackgroundThread")) {
            threadMode = ThreadMode.BackgroundThread;
        } else if (modifierString.equals("Async")) {
            threadMode = ThreadMode.Async;
        }
        return threadMode;
    }

    private SubscriberMethod constructScriberMethod(Method method, ThreadMode threadMode) {
        StringBuilder methodKeyBuilder = new StringBuilder();
        HashSet<String> eventTypesFound = new HashSet<String>();

        Class<?> eventType = method.getParameterTypes()[0];
        methodKeyBuilder.setLength(0);
        methodKeyBuilder.append(method.getName());
        methodKeyBuilder.append('>').append(eventType.getName());
        String methodKey = methodKeyBuilder.toString();
        if (eventTypesFound.add(methodKey)) {
            // Only add if not already found in a sub class
            return new SubscriberMethod(method, threadMode,
                    eventType);
        }
        return null;
    }

    /**
     * a public method start with "onEvent" and just has only one param is
     * valid.
     * 
     * @param method the target method
     * @return
     */
    private boolean isTheMethodValid(Method method) {
        return method.getName().startsWith(ON_EVENT_METHOD_NAME)
                && isPublicMethod(method.getModifiers())
                && isOnlyOneParam(method);
    }

    private boolean isPublicMethod(int modifiers) {
        return (modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0;
    }

    private boolean isOnlyOneParam(Method method) {
        return method.getParameterTypes().length == 1;
    }

    private boolean isSystemCalss(String name) {
        return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.");
    }

    static void clearCaches() {
        synchronized (methodCache) {
            methodCache.clear();
        }
    }

}
