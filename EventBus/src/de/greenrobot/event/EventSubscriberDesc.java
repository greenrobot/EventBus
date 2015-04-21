package de.greenrobot.event;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by James on 2015/2/2.<br><br>
 */
public class EventSubscriberDesc {

    private EventMethodType mEventMethodType;
    private Class<? extends IEvent> mEventClazz;
    // 用于底层反射方法名
    private String methodName = "onEvent";

    public EventSubscriberDesc(EventMethodType eventMethodType, Class<? extends IEvent> eventClazz) {
        mEventMethodType = eventMethodType;
        mEventClazz = eventClazz;
        methodName = eventMethodType.getEventMethodName();
    }

    public EventMethodType getEventMethodType() {
        return mEventMethodType;
    }

    public void setEventMethodType(EventMethodType eventMethodType) {
        mEventMethodType = eventMethodType;
    }

    // 用于底层反射
    public Class<? extends IEvent> getEventClazz() {
        return mEventClazz;
    }

    public void setEventClazz(Class<? extends IEvent> eventClazz) {
        mEventClazz = eventClazz;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj)
            return false;
        EventSubscriberDesc desc = (EventSubscriberDesc) obj;
        if (mEventMethodType == desc.getEventMethodType() && mEventClazz == desc.getEventClazz())
            return true;
        return this == obj;
    }

    public static class Builder {

        private Set<EventSubscriberDesc> mEventSubscriberDescSet;

        public Builder addEventSubscriberDesc(EventSubscriberDesc eventSubscriberDesc) {
            if (null != eventSubscriberDesc) {
                ensureList();
                mEventSubscriberDescSet.add(eventSubscriberDesc);
            }
            return this;
        }

        public Builder addEventSubscriberDesc(EventSubscriberDesc[] eventSubscriberDescs) {
            if (null != eventSubscriberDescs) {
                ensureList();
                for (int index = 0; index < eventSubscriberDescs.length; index++) {
                    EventSubscriberDesc eventSubscriberDesc = eventSubscriberDescs[index];
                    if (null != eventSubscriberDesc) {
                        mEventSubscriberDescSet.add(eventSubscriberDesc);
                    }
                }
            }
            return this;
        }

        public EventSubscriberDesc[] build() {
            if (null != mEventSubscriberDescSet) {
                List<EventSubscriberDesc> eventSubscriberDescList = new ArrayList<EventSubscriberDesc>(mEventSubscriberDescSet);
                EventSubscriberDesc[] array = new EventSubscriberDesc[eventSubscriberDescList.size()];
                for (int index = 0; index < array.length; index++) {
                    array[index] = eventSubscriberDescList.get(index);
                }
                return array;
            }
            return new EventSubscriberDesc[0];
        }

        private void ensureList() {
            if (null == mEventSubscriberDescSet)
                mEventSubscriberDescSet = new LinkedHashSet<EventSubscriberDesc>();
        }

    }

}
