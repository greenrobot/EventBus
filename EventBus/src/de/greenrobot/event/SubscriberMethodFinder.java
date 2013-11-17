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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;

class SubscriberMethodFinder {
    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC;
    private static final Map<String, List<SubscriberMethod>> methodCache = new HashMap<String, List<SubscriberMethod>>();
    private static final Map<Class<?>, Class<?>> skipMethodVerificationForClasses = new ConcurrentHashMap<Class<?>, Class<?>>();

    List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass, String eventMethodName) {
        String key = subscriberClass.getName() + '.' + eventMethodName;
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
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                String methodName = method.getName();
                if (methodName.startsWith(eventMethodName)) {
                    int modifiers = method.getModifiers();
                    if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length == 1) {
                            String modifierString = methodName.substring(eventMethodName.length());
                            ThreadMode threadMode;
                            if (modifierString.length() == 0) {
                                threadMode = ThreadMode.PostThread;
                            } else if (modifierString.equals("MainThread")) {
                                threadMode = ThreadMode.MainThread;
                            } else if (modifierString.equals("BackgroundThread")) {
                                threadMode = ThreadMode.BackgroundThread;
                            } else if (modifierString.equals("Async")) {
                                threadMode = ThreadMode.Async;
                            } else {
                                if (skipMethodVerificationForClasses.containsKey(clazz)) {
                                    continue;
                                } else {
                                    throw new EventBusException("Illegal onEvent method, check for typos: " + method);
                                }
                            }
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
                    } else if (!skipMethodVerificationForClasses.containsKey(clazz)) {
                        Log.d(EventBus.TAG, "Skipping method (not public, static or abstract): " + clazz + "."
                                + methodName);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        if (subscriberMethods.isEmpty()) {
            throw new EventBusException("Subscriber " + subscriberClass + " has no public methods called "
                    + eventMethodName);
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

    static void skipMethodVerificationFor(Class<?> clazz) {
        if (!methodCache.isEmpty()) {
            throw new IllegalStateException("This method must be called before registering anything");
        }
        skipMethodVerificationForClasses.put(clazz, clazz);
    }

    public static void clearSkipMethodVerifications() {
        skipMethodVerificationForClasses.clear();
    }
}
