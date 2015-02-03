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

import java.util.concurrent.Future;

/**
 * A representation of an Event Task which allows it to be compared, replaced or cancelled.
 */
final public class EventBusAsyncTrackedTask {

    /**
     * The future associated with this event
     */
    private Future mFuture;
    /**
     * The event the triggered the future
     */
    private AbstractEvent mEvent;

    /**
     * Gets the future
     *
     * @return The future
     */
    public Future getFuture() {
        return mFuture;
    }

    /**
     * Sets the future
     *
     * @param mFuture The future to set
     */
    public void setFuture(Future mFuture) {
        this.mFuture = mFuture;
    }

    /**
     * Gets the original event
     *
     * @return The event
     */
    public AbstractEvent getEvent() {
        return mEvent;
    }

    /**
     * Sets the original event
     *
     * @param mEvent The event
     */
    public void setEvent(AbstractEvent mEvent) {
        this.mEvent = mEvent;
    }
}
