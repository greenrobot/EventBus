package de.greenrobot.event.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.util.Log;
import de.greenrobot.event.EventBus;


/**
 * Maps throwables to texts for error dialogs. Use Config to configure the mapping.
 * 
 * @author Markus
 */
public class ExceptionToResourceMapping {

    public final Map<Class<? extends Throwable>, Integer> throwableToMsgIdMap;

    public ExceptionToResourceMapping() {
        throwableToMsgIdMap = new HashMap<Class<? extends Throwable>, Integer>();
    }

    /** Looks at the exception and its causes trying to find an ID. */
    public Integer mapThrowable(final Throwable throwable) {
        Throwable throwableToCheck = throwable;
        int depthToGo = 20;

        while (true) {
            Integer resId = mapThrowableFlat(throwableToCheck);
            if (resId != null) {
                return resId;
            } else {
                throwableToCheck = throwableToCheck.getCause();
                depthToGo--;
                if (depthToGo <= 0 || throwableToCheck == throwable || throwableToCheck == null) {
                    Log.d(EventBus.TAG, "No specific message ressource ID found for " + throwable);
                    // return config.defaultErrorMsgId;
                    return null;
                }
            }
        }

    }

    /** Mapping without checking the cause (done in mapThrowable). */
    protected Integer mapThrowableFlat(Throwable throwable) {
        Class<? extends Throwable> throwableClass = throwable.getClass();
        Integer resId = throwableToMsgIdMap.get(throwableClass);
        if (resId == null) {
            Class<? extends Throwable> closestClass = null;
            Set<Entry<Class<? extends Throwable>, Integer>> mappings = throwableToMsgIdMap.entrySet();
            for (Entry<Class<? extends Throwable>, Integer> mapping : mappings) {
                Class<? extends Throwable> candidate = mapping.getKey();
                if (candidate.isAssignableFrom(throwableClass)) {
                    if (closestClass == null || closestClass.isAssignableFrom(candidate)) {
                        closestClass = candidate;
                        resId = mapping.getValue();
                    }
                }
            }

        }
        return resId;
    }

    public ExceptionToResourceMapping addMapping(Class<? extends Throwable> clazz, int msgId) {
        throwableToMsgIdMap.put(clazz, msgId);
        return this;
    }

}
