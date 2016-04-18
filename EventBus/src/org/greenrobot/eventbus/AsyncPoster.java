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


import java.util.concurrent.Executor;

/**
 * Posts events in background.
 * 
 * @author Markus
 */
class AsyncPoster implements Runnable, Poster {

    private final PendingPostQueue queue;
    private final EventBus eventBus;
    private final Executor executor;

    AsyncPoster(EventBus eventBus) {
        this(eventBus, eventBus.getExecutorService());
    }

    AsyncPoster(EventBus eventBus, Executor executor) {
        this.eventBus = eventBus;
        this.executor = executor;
        queue = new PendingPostQueue();
    }

    public void enqueue(Subscription subscription, Object event) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        queue.enqueue(pendingPost);
        executor.execute(this);
    }

    @Override
    public void run() {
        PendingPost pendingPost = queue.poll();
        if(pendingPost == null) {
            throw new IllegalStateException("No pending post available");
        }
        eventBus.invokeSubscriber(pendingPost);
    }

}
