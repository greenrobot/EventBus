/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
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
package org.greenrobot.eventbus;

/**
 * This Event is posted by EventBus when an exception occurs inside a subscriber's event handling method.
 * 
 * @author Markus
 */
public final class SubscriberExceptionEvent {
    /** The {@link EventBus} instance to with the original event was posted to. */
    public final EventBus eventBus;

    /** The Throwable thrown by a subscriber. */
    public final Throwable throwable;

    /** The original event that could not be delivered to any subscriber. */
    public final Object causingEvent;

    /** The subscriber that threw the Throwable. */
    public final Object causingSubscriber;

    public SubscriberExceptionEvent(EventBus eventBus, Throwable throwable, Object causingEvent,
            Object causingSubscriber) {
        this.eventBus = eventBus;
        this.throwable = throwable;
        this.causingEvent = causingEvent;
        this.causingSubscriber = causingSubscriber;
    }

}
