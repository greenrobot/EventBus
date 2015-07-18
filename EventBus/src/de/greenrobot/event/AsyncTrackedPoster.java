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
package de.greenrobot.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import de.greenrobot.event.models.AbstractEvent;
import de.greenrobot.event.models.EventBusAsyncTrackedTask;

/**
 * Posts events in background.
 * 
 * @author Pedro Vicente
 */
class AsyncTrackedPoster implements Runnable {

    private final PendingPostQueue queue;

    private final EventBus eventBus;

    /**
     * List of current submitted/executing tasks of EventBus
     */
    private ConcurrentHashMap<String, List<EventBusAsyncTrackedTask>> mEventBusTaskList = null;

    AsyncTrackedPoster(EventBus eventBus) {
        this.eventBus = eventBus;
        mEventBusTaskList = new ConcurrentHashMap<String, List<EventBusAsyncTrackedTask>>();
        queue = new PendingPostQueue();
    }

    public void enqueue(Subscription subscription, Object event) {
        if (event instanceof AbstractEvent) {
            AbstractEvent abstractEvent = ((AbstractEvent) event);
            // Add this to have a single identification for each event that targets a subscriber to be able to remove
            // the specific one
            abstractEvent.setSubscriberHashCode(subscription.subscriber.hashCode());
            submitEvent(subscription, abstractEvent);
        } else {
            PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
            queue.enqueue(pendingPost);
            EventBus.executorService.execute(this);
        }

    }

    @Override
    public void run() {
        PendingPost pendingPost = queue.poll();
        if (pendingPost == null) {
            throw new IllegalStateException("No pending post available");
        }
        eventBus.invokeSubscriber(pendingPost);

        if (pendingPost.event instanceof AbstractEvent) {
            // remove the event from the tracker after finishing execution
            remove((AbstractEvent) pendingPost.event);
        }
    }

    public void submitEvent(Subscription subscription, AbstractEvent event) {

        // There is at least one event of this type.
        if (mEventBusTaskList.containsKey(event.getEventType())) {

            List<EventBusAsyncTrackedTask> taskList = mEventBusTaskList.get(event.getEventType());
            Iterator<EventBusAsyncTrackedTask> it = taskList.iterator();

            while (it.hasNext()) {
                EventBusAsyncTrackedTask task = it.next();
                AbstractEvent storedEvent = task.getEvent();

                if (AbstractEvent.compareEventUniqueCode(storedEvent, event)
                        && AbstractEvent.compareEventSubscribersHashCode(storedEvent, event)) {

                    Future future = task.getFuture();

                    switch (event.getCollisionBehavior()) {
                    case IGNORE_NEW_EVENT:
                        if (!future.isCancelled() && !future.isDone()) {
                            return;
                        } else {
                            // removing as it has finished so should not be here anymore (just to be safe)
                            taskList.remove(task);
                        }
                        break;
                    case REPLACE_WITH_NEW_EVENT:
                    default:
                        if (!future.isCancelled() && !future.isDone()) {
                            future.cancel(true);
                        }
                        taskList.remove(task);
                        break;

                    }
                }
            }

            PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
            queue.enqueue(pendingPost);
            Future future = EventBus.executorService.submit(this);

            EventBusAsyncTrackedTask task = new EventBusAsyncTrackedTask();
            task.setFuture(future);
            task.setEvent(event);

            taskList.add(task);

        } else {
            // If its a new type of event, add it to the map with a new list

            PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
            queue.enqueue(pendingPost);
            Future future = EventBus.executorService.submit(this);

            EventBusAsyncTrackedTask task = new EventBusAsyncTrackedTask();
            task.setFuture(future);
            task.setEvent(event);

            List<EventBusAsyncTrackedTask> taskList = Collections
                    .synchronizedList(new ArrayList<EventBusAsyncTrackedTask>());
            taskList.add(task);

            mEventBusTaskList.put(event.getEventType(), taskList);

        }
    }

    /**
     * Remove an event/runnable from the current tracking. It is used when a method has finished executing.
     *
     * @param event The event to remove
     */
    private void remove(AbstractEvent event) {

        if (mEventBusTaskList.containsKey(event.getEventType())) {
            List<EventBusAsyncTrackedTask> tasks = mEventBusTaskList.get(event.getEventType());
            Iterator<EventBusAsyncTrackedTask> it = tasks.iterator();
            while (it.hasNext()) {
                EventBusAsyncTrackedTask task = it.next();
                AbstractEvent storedEvent = task.getEvent();

                boolean remove = false;
                boolean breakCycle = false;

                if (AbstractEvent.compareEventUniqueCode(storedEvent, event)
                        && AbstractEvent.compareEventSubscribersHashCode(storedEvent, event)) {
                    tasks.remove(task);
                    break;
                }
            }

            // If there are no events of that type, clear it from the map.
            if (tasks.size() == 0) {
                mEventBusTaskList.remove(event.getEventType());
            }
        }
    }

    /**
     * Cancels and removes an event from the tracking
     *
     * @param eventToCancel The event to cancel
     */
    public int cancel(AbstractEvent eventToCancel) {
        int cancelledEvent = 0;

        if (mEventBusTaskList.containsKey(eventToCancel.getEventType())) {
            List<EventBusAsyncTrackedTask> tasks = mEventBusTaskList.get(eventToCancel.getEventType());
            Iterator<EventBusAsyncTrackedTask> it = tasks.iterator();
            while (it.hasNext()) {
                EventBusAsyncTrackedTask task = it.next();
                AbstractEvent storedEvent = task.getEvent();

                //Here the subscriber code is not validated in purpose to cancel all events from all subscribers
                if (AbstractEvent.compareEventUniqueCode(storedEvent, eventToCancel)) {

                    Future future = task.getFuture();
                    if (!future.isDone()) {
                        future.cancel(true);
                    }
                    tasks.remove(storedEvent);
                    cancelledEvent++;
                }
            }

            // If there are no events of that type, clear it from the map.
            if (tasks.size() == 0) {
                mEventBusTaskList.remove(eventToCancel.getEventType());
            }
        }

        return cancelledEvent;
    }

}
