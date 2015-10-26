package de.greenrobot.event.util;

import android.content.res.Resources;
import de.greenrobot.event.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ErrorDialogConfig {
    final Resources resources;
    final int defaultTitleId;
    final int defaultErrorMsgId;
    final ExceptionToResourceMapping mapping;

    EventBus eventBus;
    boolean logExceptions = true;
    String tagForLoggingExceptions;
    int defaultDialogIconId;
    Class<?> defaultEventTypeOnDialogClosed;

    public ErrorDialogConfig(Resources resources, int defaultTitleId, int defaultMsgId) {
        this.resources = resources;
        this.defaultTitleId = defaultTitleId;
        this.defaultErrorMsgId = defaultMsgId;
        mapping = new ExceptionToResourceMapping();
    }

    public ErrorDialogConfig addMapping(Class<? extends Throwable> clazz, int msgId) {
        mapping.addMapping(clazz, msgId);
        return this;
    }

    public int getMessageIdForThrowable(final Throwable throwable) {
        Integer resId = mapping.mapThrowable(throwable);
        if (resId != null) {
            return resId;
        } else {
            eventBus.getLogger().d(EventBus.TAG, "No specific message resource ID found for " + throwable, null);
            return defaultErrorMsgId;
        }
    }

    public void setDefaultDialogIconId(int defaultDialogIconId) {
        this.defaultDialogIconId = defaultDialogIconId;
    }

    public void setDefaultEventTypeOnDialogClosed(Class<?> defaultEventTypeOnDialogClosed) {
        this.defaultEventTypeOnDialogClosed = defaultEventTypeOnDialogClosed;
    }

    public void disableExceptionLogging() {
        logExceptions = false;
    }

    public void setTagForLoggingExceptions(String tagForLoggingExceptions) {
        this.tagForLoggingExceptions = tagForLoggingExceptions;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /** eventBus!=null ? eventBus: EventBus.getDefault() */
    EventBus getEventBus() {
        return eventBus!=null ? eventBus: EventBus.getDefault();
    }


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
                        eventBus.getLogger().d(EventBus.TAG, "No specific message resource ID found for " + throwable, null);
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
                Set<Map.Entry<Class<? extends Throwable>, Integer>> mappings = throwableToMsgIdMap.entrySet();
                for (Map.Entry<Class<? extends Throwable>, Integer> mapping : mappings) {
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
}
