package de.greenrobot.event;

public class BackgroundExceptionEvent {
    public final Throwable throwable;
    public final Object causingEvent;
    public final Object causingSubscriber;
    
    public BackgroundExceptionEvent(Throwable throwable, Object causingEvent, Object causingSubscriber) {
        this.throwable = throwable;
        this.causingEvent = causingEvent;
        this.causingSubscriber = causingSubscriber;
    }

}
