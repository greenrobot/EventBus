package de.greenrobot.event;

public enum ThreadMode {
    /** Subscriber will be called in the same thread, which is posting the event. */
    PostThread,
    /** Subscriber will be called in Android's main thread (sometimes referred to as UI thread). */
    MainThread,
    /** Subscriber will be called in a background thread */
    BackgroundThread
}