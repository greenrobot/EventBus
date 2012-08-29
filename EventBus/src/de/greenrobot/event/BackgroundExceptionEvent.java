/*
 * Copyright (C) 2012 Markus Junginger, greenrobot (http://greenrobot.de)
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
package de.greenrobot.event;

/**
 * TODO Implement a consistent exception handling. One part of this could be posting this class as event when the
 * background thread or async poster step on an exception.
 * 
 * @author Markus
 */
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
