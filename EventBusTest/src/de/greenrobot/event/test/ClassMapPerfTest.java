package de.greenrobot.event.test;

import junit.framework.TestCase;

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
