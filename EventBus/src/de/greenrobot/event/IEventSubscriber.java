package de.greenrobot.event;

/**
 * Created by James on 2015/2/2.<br><br>
 *
 * @Override public EventSubscriberDesc[] getEventSubscriberDescList() {
 *          EventSubscriberDesc.Builder builder = new EventSubscriberDesc.Builder();
 *          builder.addEventSubscriberDesc(new EventSubscriberDesc(EventMethodType.EVENT_MAIN_THREAD,EventAlarmListRefresh.class));
 *          return builder.build();
 * }
 */
public interface IEventSubscriber {

    // JNI callback method
    EventSubscriberDesc[] getEventSubscriberDescList();

}
