package de.greenrobot.event;

/**
 * Created by James on 2015/2/2.<br><br>
 */
public enum EventMethodType {

    EVENT_DEFAULT("onEvent"),
    EVENT_MAIN_THREAD("onEventMainThread"),
    EVENT_BACKGROUND_THREAD("onEventBackgroundThread"),
    EVENT_ASYNC("onEventAsync");

    private String mEventMethodName;

    EventMethodType(String eventMethodName) {
        mEventMethodName = eventMethodName;
    }

    public String getEventMethodName() {
        return mEventMethodName;
    }

}
