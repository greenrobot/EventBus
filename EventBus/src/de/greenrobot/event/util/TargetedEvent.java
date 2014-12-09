package de.greenrobot.event.util;

import android.text.TextUtils;

import java.util.LinkedList;

/**
 * Author: landerlyoung@gmail.com
 * Date:   2014-12-09
 * Time:   15:57
 * Life with passion. Code with creativity!
 *
 * A {@Code TargetedEvent} is a event with target.
 * Youg <b>MUST</b> get a {@Code TargetedEvent} from cache by {@link #obtain()}  or
 * {@link #obtain(Object event, String target)}, and {@link #recycle()} after done with it.</br>
 * The Constructor is not visible.</br>
 * Method {@link #matchesTargetClass(Class<?> targetClazz, String targetClassName)} is used to determine
 * whether given target matches the class.
 */
public final class TargetedEvent {
    public Object event;
    public String target;

    private TargetedEvent() {
    }

    private TargetedEvent(Object event, String target) {
        this.event = event;
        this.target = target;
    }

    public static TargetedEvent obtain() {
        return obtain(null, null);
    }

    public static TargetedEvent obtain(Object event, String target) {
        TargetedEvent t;
        synchronized (mCache) {
            t = mCache.poll();
        }
        if (t == null) {
            t = new TargetedEvent(event, target);
        } else {
            t.event = event;
            t.target = target;
        }
        return t;
    }

    public static Object getEvent(TargetedEvent targetedEvent) {
        if (targetedEvent == null) {
            return null;
        } else {
            return targetedEvent.event;
        }
    }


    public static String getTarget(TargetedEvent targetedEvent) {
        if (targetedEvent == null) {
            return null;
        } else {
            return targetedEvent.target;
        }
    }

    /**
     * util method to determine whether the given class name
     * point to the class type
     *
     * @param targetClazz     class type
     * @param targetClassName given class name
     * @return true if matches
     */
    public static boolean matchesTargetClass(Class<?> targetClazz, String targetClassName) {
        return targetClassName == null ||
                targetClassName.equals(targetClazz.getName()) ||
                targetClassName.equals(targetClazz.getCanonicalName());
    }

    private static final int MAX_CACHE_SIZE = 32;
    private static final LinkedList<TargetedEvent> mCache = new LinkedList<TargetedEvent>();

    /**
     * recycle this instance to cache.
     * You <b>SHALL NOT</b> touch the instance after recycle.
     */
    public void recycle() {
        this.event = null;
        this.target = null;
        synchronized (mCache) {
            if (mCache.size() < MAX_CACHE_SIZE) {
                mCache.add(this);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof TargetedEvent) {
            return event == (((TargetedEvent) o).event) &&
                    TextUtils.equals(target, ((TargetedEvent) o).target);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashcode = 0;
        if (event != null) {
            hashcode += event.hashCode();
        }
        if (target != null) {
            target += target.hashCode();
        }
        return hashcode;
    }
}
