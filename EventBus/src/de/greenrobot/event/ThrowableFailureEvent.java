package de.greenrobot.event;

/** A generic failure event, which can be used by apps to propagate thrown exceptions. */
public class ThrowableFailureEvent {
    public final Throwable throwable;

    public ThrowableFailureEvent(Throwable throwable) {
        this.throwable = throwable;
    }

}
