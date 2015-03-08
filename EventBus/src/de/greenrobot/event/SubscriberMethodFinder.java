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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class SubscriberMethodFinder {
    /*
     * In newer class files, compilers may add methods. Those are called bridge or synthetic methods.
     * EventBus must ignore both. There modifiers are not public but defined in the Java class file format:
     * http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.6-200-A.1
     */
    private static final int BRIDGE = 0x40;
    private static final int SYNTHETIC = 0x1000;

    private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;
    private static final Map<String, List<SubscriberMethod>> METHOD_CACHE = new HashMap<String, List<SubscriberMethod>>();

    /** Optional generated index without entries from subscribers super classes */
    private static final SubscriberIndex INDEX;

    static {
        SubscriberIndex newIndex = null;
        try {
            Class<?> clazz = Class.forName("de.greenrobot.event.MyGeneratedSubscriberIndex");
            newIndex = (SubscriberIndex) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            Log.d(EventBus.TAG, "No subscriber index available, reverting to dynamic look-up");
            // Fine
        } catch (Exception e) {
            Log.w(EventBus.TAG, "Could not init subscriber index, reverting to dynamic look-up", e);
        }
        INDEX = newIndex;
    }

    private final boolean strictMethodVerification;

    SubscriberMethodFinder(boolean strictMethodVerification) {
        this.strictMethodVerification = strictMethodVerification;
    }

    List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
        String key = subscriberClass.getName();
        List<SubscriberMethod> subscriberMethods;
        synchronized (METHOD_CACHE) {
            subscriberMethods = METHOD_CACHE.get(key);
        }
        if (subscriberMethods != null) {
            return subscriberMethods;
        }
        if (INDEX != null) {
            subscriberMethods = findSubscriberMethodsWithIndex(subscriberClass);
        } else {
            subscriberMethods = findSubscriberMethodsWithReflection(subscriberClass);
        }
        if (subscriberMethods.isEmpty()) {
            throw new EventBusException("Subscriber " + subscriberClass
                    + " and its super classes have no public methods with the @Subscribe annotation");
        } else {
            synchronized (METHOD_CACHE) {
                METHOD_CACHE.put(key, subscriberMethods);
            }
            return subscriberMethods;
        }
    }

    private List<SubscriberMethod> findSubscriberMethodsWithIndex(Class<?> subscriberClass) {
        Class<?> clazz = subscriberClass;
        while (clazz != null) {
            SubscriberMethod[] array = INDEX.getSubscribersFor(clazz);
            if (array != null && array.length > 0) {
                List<SubscriberMethod> subscriberMethods = new ArrayList<SubscriberMethod>();
                for (SubscriberMethod subscriberMethod : array) {
                    subscriberMethods.add(subscriberMethod);
                }
                return subscriberMethods;
            } else {
                String name = clazz.getName();
                if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
                    // Skip system classes, this just degrades performance
                    break;
                }
                clazz = clazz.getSuperclass();
            }
        }
        return Collections.EMPTY_LIST;
    }

    private List<SubscriberMethod> findSubscriberMethodsWithReflection(Class<?> subscriberClass) {
        List<SubscriberMethod> subscriberMethods = new ArrayList<SubscriberMethod>();
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
                int modifiers = method.getModifiers();
                if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 1) {
                        Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                        if (subscribeAnnotation != null) {
                            String methodName = method.getName();
                            Class<?> eventType = parameterTypes[0];
                            methodKeyBuilder.setLength(0);
                            methodKeyBuilder.append(methodName);
                            methodKeyBuilder.append('>').append(eventType.getName());

                            String methodKey = methodKeyBuilder.toString();
                            if (eventTypesFound.add(methodKey)) {
                                // Only add if not already found in a sub class
                                ThreadMode threadMode = subscribeAnnotation.threadMode();
                                subscriberMethods.add(new SubscriberMethod(method, threadMode, eventType));
                            }
                        }
                    } else if (strictMethodVerification) {
                        if (method.isAnnotationPresent(Subscribe.class)) {
                            String methodName = name + "." + method.getName();
                            throw new EventBusException("@Subscribe method " + methodName +
                                    "must have exactly 1 parameter but has " + parameterTypes.length);
                        }
                    }
                } else if (strictMethodVerification) {
                    if (method.isAnnotationPresent(Subscribe.class)) {
                        String methodName = name + "." + method.getName();
                        throw new EventBusException(methodName +
                                " is a illegal @Subscribe method: must be public, non-static, and non-abstract");
                    }

                }
            }

            clazz = clazz.getSuperclass();
        }
        return subscriberMethods;
    }

    static void clearCaches() {
        synchronized (METHOD_CACHE) {
            METHOD_CACHE.clear();
        }
    }

}
