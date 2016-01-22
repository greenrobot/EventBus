package org.greenrobot.eventbus;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Subscribe {
    ThreadMode threadMode() default ThreadMode.POSTING;

    /**
     * If true, delivers the most recent sticky event (posted with
     * {@link EventBus#postSticky(Object)}) to this subscriber (if event available).
     */
    boolean sticky() default false;

    /** Subscriber priority to influence the order of event delivery.
     * Within the same delivery thread ({@link ThreadMode}), higher priority subscribers will receive events before
     * others with a lower priority. The default priority is 0. Note: the priority does *NOT* affect the order of
     * delivery among subscribers with different {@link ThreadMode}s! */
    int priority() default 0;
}

