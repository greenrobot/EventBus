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
import de.greenrobot.event.annotations.Subscribe;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class SubscriberMethodFinder {

    /*
     * In newer class files, compilers may add methods. Those are called bridge or synthetic methods.
     * EventBus must ignore both. Their modifiers are not public but defined in the Java class file format:
     * http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.6-200-A.1
     */
    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;

    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;
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
        HashSet<String> eventTypesFound = new HashSet<String>();
        StringBuilder methodKeyBuilder = new StringBuilder();
        while (clazz != null) {
            String name = clazz.getName();
            if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
                // Skip system classes, this just degrades performance
                break;
            }

            // Starting with EventBus 2.2 we enforced methods to be public (might change with annotations again)
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                String methodName = method.getName();

                // Now we find acceptable methods via annotations
                if (method.isAnnotationPresent(Subscribe.class)) {
                    Log.e("EventBus", "Annotation is present for " + methodName);
                    int modifiers = method.getModifiers();
                    if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length == 1) {
                            Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                            if (subscribeAnnotation != null && subscribeAnnotation.threadMode() != null) {
                                ThreadMode threadMode = subscribeAnnotation.threadMode();
                                Class<?> eventType = parameterTypes[0];

                                methodKeyBuilder.setLength(0);
                                methodKeyBuilder.append(methodName);
                                methodKeyBuilder.append('>').append(eventType.getName());
                                String methodKey = methodKeyBuilder.toString();
                                if (eventTypesFound.add(methodKey)) {
                                    // Only add if not already found in a sub class
                                    subscriberMethods.add(new SubscriberMethod(method, threadMode, eventType));
                                }
                            }
                        }
                    }

                } else {
                    Log.e("EventBus", "Annotation is NOT present for " + methodName);
                }
            }

            clazz = clazz.getSuperclass();
        }
        if (subscriberMethods.isEmpty()) {
            throw new EventBusException("Subscriber " + subscriberClass
                    + " has no public methods called with the @Subscribe annotation");
        } else {
            synchronized (methodCache) {
                methodCache.put(key, subscriberMethods);
            }
            return subscriberMethods;
        }
    }

    static void clearCaches() {
        synchronized (methodCache) {
            methodCache.clear();
        }
    }

}
