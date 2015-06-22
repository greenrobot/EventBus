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

    private final boolean strictMethodVerification;
    private final boolean ignoreGeneratedIndex;

    SubscriberMethodFinder(boolean strictMethodVerification, boolean ignoreGeneratedIndex) {
        this.strictMethodVerification = strictMethodVerification;
        this.ignoreGeneratedIndex = ignoreGeneratedIndex;
    }

    List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass, boolean forceReflection) {
        String key = subscriberClass.getName();
        List<SubscriberMethod> subscriberMethods;
        synchronized (METHOD_CACHE) {
            subscriberMethods = METHOD_CACHE.get(key);
        }
        if (subscriberMethods != null) {
            return subscriberMethods;
        }
        if (!ignoreGeneratedIndex && !forceReflection) {
            subscriberMethods = findUsingInfo(subscriberClass);
        } else {
            subscriberMethods = findUsingReflection(subscriberClass);
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

    private List<SubscriberMethod> findUsingInfo(Class<?> subscriberClass) {
        FindState findState = new FindState();
        findState.initForSubscriber(subscriberClass);
        while (findState.clazz != null) {
            SubscriberInfo info = getSubscriberInfo(subscriberClass);
            if (info != null) {
                SubscriberInfo.Data subscriberData = info.getSubscriberData();
                SubscriberMethod[] array = subscriberData.subscriberMethods;
                for (SubscriberMethod subscriberMethod : array) {
                    if (findState.checkAdd(subscriberMethod.method, subscriberMethod.eventType)) {
                        findState.subscriberMethods.add(subscriberMethod);
                    }
                }
            } else {
                findUsingReflectionInSingleClass(findState);
            }
            findState.nextClass();
        }
        return findState.subscriberMethods;
    }

    private SubscriberInfo getSubscriberInfo(Class<?> subscriberClass) {
        SubscriberInfo info = null;
        String infoClass = subscriberClass.getName().replace('$', '_') + "_EventBusInfo";
        try {
            Class<?> aClass = Class.forName(infoClass);
            Object object = aClass.newInstance();
            if (object instanceof SubscriberInfo) {
                info = (SubscriberInfo) object;
            }
        } catch (ClassNotFoundException e) {
            // TODO don't try again
        } catch (Exception e) {
            throw new EventBusException("Could not get infos for " + subscriberClass, e);
        }
        return info;
    }

    private List<SubscriberMethod> findUsingReflection(Class<?> subscriberClass) {
        FindState findState = new FindState();
        findState.initForSubscriber(subscriberClass);
        while (findState.clazz != null) {
            findUsingReflectionInSingleClass(findState);
            findState.nextClass();
        }
        return findState.subscriberMethods;
    }

    private void findUsingReflectionInSingleClass(FindState findState) {
        Method[] methods = findState.clazz.getDeclaredMethods();
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                    if (subscribeAnnotation != null) {
                        Class<?> eventType = parameterTypes[0];
                        if (findState.checkAdd(method, eventType)) {
                            ThreadMode threadMode = subscribeAnnotation.threadMode();
                            findState.subscriberMethods.add(new SubscriberMethod(method, eventType, threadMode,
                                    subscribeAnnotation.priority(), subscribeAnnotation.sticky()));
                        }
                    }
                } else if (strictMethodVerification) {
                    if (method.isAnnotationPresent(Subscribe.class)) {
                        String methodName = findState.clazzName + "." + method.getName();
                        throw new EventBusException("@Subscribe method " + methodName +
                                "must have exactly 1 parameter but has " + parameterTypes.length);
                    }
                }
            } else if (strictMethodVerification) {
                if (method.isAnnotationPresent(Subscribe.class)) {
                    String methodName = findState.clazzName + "." + method.getName();
                    throw new EventBusException(methodName +
                            " is a illegal @Subscribe method: must be public, non-static, and non-abstract");
                }
            }
        }
    }

    static void clearCaches() {
        synchronized (METHOD_CACHE) {
            METHOD_CACHE.clear();
        }
    }

    class FindState {
        final List<SubscriberMethod> subscriberMethods = new ArrayList<SubscriberMethod>();
        final HashSet<String> eventTypesFound = new HashSet<String>();
        final StringBuilder methodKeyBuilder = new StringBuilder();
        Class<?> subscriberClass;
        Class<?> clazz;
        String clazzName;

        void initForSubscriber(Class<?> subscriberClass) {
            this.subscriberClass = clazz = subscriberClass;
        }

        void recycle() {
            subscriberMethods.clear();
            methodKeyBuilder.setLength(0);
            eventTypesFound.clear();
        }

        boolean checkAdd(Method method, Class<?> eventType) {
            methodKeyBuilder.setLength(0);
            methodKeyBuilder.append(method.getName());
            methodKeyBuilder.append('>').append(eventType.getName());

            String methodKey = methodKeyBuilder.toString();
            return eventTypesFound.add(methodKey);
        }

        void nextClass() {
            clazz = clazz.getSuperclass();
            clazzName = clazz.getName();
            /** Skip system classes, this just degrades performance. */
            if (clazzName.startsWith("java.") || clazzName.startsWith("javax.") || clazzName.startsWith("android.")) {
                clazz = null;
                clazzName = null;
            }
        }
    }

}
