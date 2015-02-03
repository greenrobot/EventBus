/*
 * Copyright (C) 2015 Pedro Vicente (neteinstein@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.greenrobot.event.models;

/**
 * Generic event to be extended by events when using the AsyncTracked mode. It allows each event to define its behavior
 */
public class AbstractEvent {

    /**
     * Collision Behavior options
     */
    public enum EventCollisionBehavior {
        IGNORE_NEW_EVENT, REPLACE_WITH_NEW_EVENT
    }

    /**
     * Default collision behavior
     */
    protected EventCollisionBehavior mCollisionBehavior = EventCollisionBehavior.REPLACE_WITH_NEW_EVENT;

    private long subscriberHashCode = 0l;

    /**
     * Compare AbstractEventRequests unique event codes to check if the event is considered "the same"
     *
     * @param a An AbstractEventRequest
     * @param b An AbstractEventRequest
     * @return true if they are the same, false if not.
     */
    public static boolean compareEventUniqueCode(AbstractEvent a, AbstractEvent b) {
        String eventCodeA = a.getEventUniqueCode();
        String eventCodeB = b.getEventUniqueCode();

        if (eventCodeA == null || eventCodeB == null) {
            return true;
        } else if (eventCodeA == null || eventCodeB == null) {
            return false;
        }

        return eventCodeA.equals(eventCodeB);
    }

    /**
     * Compare AbstractEventRequests subscriber codes
     *
     * @param a An AbstractEventRequest
     * @param b An AbstractEventRequest
     * @return true if they are the same, false if not.
     */
    public static boolean compareEventSubscribersHashCode(AbstractEvent a, AbstractEvent b) {
        long eventCodeA = a.getSubscriberHashCode();
        long eventCodeB = b.getSubscriberHashCode();

        return eventCodeA == eventCodeB;
    }

    /**
     * This will return the class name which can be used as a "type" of event
     */
    public String getEventType() {
        String className = getClass().getCanonicalName();

        return className;
    }

    /**
     * This method should be overriden by events if the same event class triggers different events that should run in
     * parallel
     */
    public String getEventUniqueCode() {
        return getEventType();
    }

    /**
     * What happens if an event with the same type and hashcode is submitted
     *
     * @return enum with the behaviour
     */
    public EventCollisionBehavior getCollisionBehavior() {
        return mCollisionBehavior;
    }

    /**
     * Override the default collision behaviour if necessary
     *
     * @param collisionBehavior The new collision behavior
     */
    public void setCollisionBehavior(EventCollisionBehavior collisionBehavior) {
        this.mCollisionBehavior = collisionBehavior;
    }

    public long getSubscriberHashCode() {
        return subscriberHashCode;
    }

    public void setSubscriberHashCode(long subscriberHashCode) {
        this.subscriberHashCode = subscriberHashCode;
    }

    /**
     * Prints readable object in readable format
     *
     * @return The object info in readable format
     */
    public String toString() {

        return getEventType()
                + " with  EventCode:"
                + getEventUniqueCode()
                + " CollisionBehavior:"
                + getCollisionBehavior();
    }
}
