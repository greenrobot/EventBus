package org.greenrobot.eventbus.logger;

import org.greenrobot.eventbus.SubscriberMethod;

/**
 * Information that will be past to logger to allow customization using a Handler
 */
public class EventLoggerParameter {


    public enum EventLoggerParameterType {

        /**
         * Register of subscriber
         */
        REGISTER,

        /**
         * Unregister of subscriber
         */
        UNREGISTER,

        /**
         * Starting to post an event
         */
        POST_EVENT,

        /**
         * Delivering and event to an specific subscriber within the process of posting
         */
        POSTING_EVENT,

        /**
         * No subscriber found for the event
         */
        POST_NO_SUBSCRIBER,

    }

    private EventLoggerParameterType parameterType;

    private Object mSubscriber;
    private SubscriberMethod mSubscriberMethod;
    private Object mEvent;


    public EventLoggerParameter(EventLoggerParameterType parameterType, Object subscriber, SubscriberMethod subscriberMethod) {
        this(parameterType,subscriber,subscriberMethod,null);
    }

    public EventLoggerParameter(EventLoggerParameterType parameterType, Object subscriber, SubscriberMethod subscriberMethod, Object event) {
        this.parameterType = parameterType;
        mSubscriber = subscriber;
        mSubscriberMethod = subscriberMethod;
        mEvent = event;
    }

    public EventLoggerParameterType getParameterType() {
        return parameterType;
    }

    public void setParameterType(EventLoggerParameterType parameterType) {
        this.parameterType = parameterType;
    }

    public Object getSubscriber() {
        return mSubscriber;
    }

    public void setSubscriber(Object subscriber) {
        mSubscriber = subscriber;
    }

    public SubscriberMethod getSubscriberMethod() {
        return mSubscriberMethod;
    }

    public void setSubscriberMethod(SubscriberMethod subscriberMethod) {
        mSubscriberMethod = subscriberMethod;
    }

    public Object getEvent() {
        return mEvent;
    }

    public void setEvent(Object event) {
        mEvent = event;
    }
}
