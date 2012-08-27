package de.greenrobot.event;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

final class PostViaHandler extends Handler {

    PostViaHandler(Looper looper) {
        super(looper);
    }

    void enqueue(Object event, Subscription subscription) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(event, subscription);
        Message message = obtainMessage();
        message.obj = pendingPost;
        if (!sendMessage(message)) {
            throw new RuntimeException("Could not send handler message");
        }
    }

    @Override
    public void handleMessage(Message msg) {
        PendingPost pendingPost = (PendingPost) msg.obj;
        Object event = pendingPost.event;
        Subscription subscription = pendingPost.subscription;
        PendingPost.releasePendingPost(pendingPost);
        EventBus.postToSubscribtion(subscription, event);
    }

}