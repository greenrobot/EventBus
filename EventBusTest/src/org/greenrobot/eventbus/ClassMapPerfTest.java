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

package org.greenrobot.eventbus;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Just to verify testHashMapClassObject is fastest. Ignore this test.
 */
public class ClassMapPerfTest /* extends TestCase */ {

    static final int COUNT = 10000000;
    static final Class CLAZZ = ClassMapPerfTest.class;

    public void testHashMapClassObject() {
        Map<Class, Class> map = new HashMap<Class, Class>();
        for (int i = 0; i < COUNT; i++) {
            Class oldValue = map.put(CLAZZ, CLAZZ);
            Class value = map.get(CLAZZ);
        }
    }

    public void testIdentityHashMapClassObject() {
        Map<Class, Class> map = new IdentityHashMap<Class, Class>();
        for (int i = 0; i < COUNT; i++) {
            Class oldValue = map.put(CLAZZ, CLAZZ);
            Class value = map.get(CLAZZ);
        }
    }

    public void testHashMapClassName() {
        Map<String, Class> map = new HashMap<String, Class>();
        for (int i = 0; i < COUNT; i++) {
            Class oldValue = map.put(CLAZZ.getName(), CLAZZ);
            Class value = map.get(CLAZZ.getName());
        }
    }

}
