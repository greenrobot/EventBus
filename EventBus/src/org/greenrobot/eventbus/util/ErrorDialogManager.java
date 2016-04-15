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

package org.greenrobot.eventbus.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

/**
 * Central class for app that want to use event based error dialogs.<br/>
 * <br/>
 * How to use:
 * <ol>
 * <li>Set the {@link #factory} to configure dialogs for your app, typically in {@link Application#onCreate()}</li>
 * <li>Use one of {@link #attachTo(Activity)}, {@link #attachTo(Activity, boolean)} or
 * {@link #attachTo(Activity, boolean, Bundle)} in your Activity, typically in onCreate.</li>
 * </ol>
 * 
 * For more complex mappings, you can supply your own {@link ErrorDialogFragmentFactory}.
 * 
 * @author Markus
 */
public class ErrorDialogManager {

    public static class SupportManagerFragment extends Fragment {
        protected boolean finishAfterDialog;
        protected Bundle argumentsForErrorDialog;
        private EventBus eventBus;
        private boolean skipRegisterOnNextResume;
        private Object executionScope;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            eventBus = ErrorDialogManager.factory.config.getEventBus();
            eventBus.register(this);
            skipRegisterOnNextResume = true;
        }

        @Override
        public void onResume() {
            super.onResume();
            if (skipRegisterOnNextResume) {
                // registered in onCreate, skip registration in this run
                skipRegisterOnNextResume = false;
            } else {
                eventBus = ErrorDialogManager.factory.config.getEventBus();
                eventBus.register(this);
            }
        }

        @Override
        public void onPause() {
            eventBus.unregister(this);
            super.onPause();
        }

        public void onEventMainThread(ThrowableFailureEvent event) {
            if (!isInExecutionScope(executionScope, event)) {
                return;
            }
            checkLogException(event);
            // Execute pending commits before finding to avoid multiple error fragments being shown
            FragmentManager fm = getFragmentManager();
            fm.executePendingTransactions();

            DialogFragment existingFragment = (DialogFragment) fm.findFragmentByTag(TAG_ERROR_DIALOG);
            if (existingFragment != null) {
                // Just show the latest error
                existingFragment.dismiss();
            }

            android.support.v4.app.DialogFragment errorFragment = (android.support.v4.app.DialogFragment) factory
                    .prepareErrorFragment(event, finishAfterDialog, argumentsForErrorDialog);
            if (errorFragment != null) {
                errorFragment.show(fm, TAG_ERROR_DIALOG);
            }
        }

        public static void attachTo(Activity activity, Object executionScope, boolean finishAfterDialog,
                Bundle argumentsForErrorDialog) {
            FragmentManager fm = ((FragmentActivity) activity).getSupportFragmentManager();
            SupportManagerFragment fragment = (SupportManagerFragment) fm.findFragmentByTag(TAG_ERROR_DIALOG_MANAGER);
            if (fragment == null) {
                fragment = new SupportManagerFragment();
                fm.beginTransaction().add(fragment, TAG_ERROR_DIALOG_MANAGER).commit();
                fm.executePendingTransactions();
            }
            fragment.finishAfterDialog = finishAfterDialog;
            fragment.argumentsForErrorDialog = argumentsForErrorDialog;
            fragment.executionScope = executionScope;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class HoneycombManagerFragment extends android.app.Fragment {
        protected boolean finishAfterDialog;
        protected Bundle argumentsForErrorDialog;
        private EventBus eventBus;
        private Object executionScope;

        @Override
        public void onResume() {
            super.onResume();
            eventBus = ErrorDialogManager.factory.config.getEventBus();
            eventBus.register(this);
        }

        @Override
        public void onPause() {
            eventBus.unregister(this);
            super.onPause();
        }

        public void onEventMainThread(ThrowableFailureEvent event) {
            if (!isInExecutionScope(executionScope, event)) {
                return;
            }
            checkLogException(event);

            // Execute pending commits before finding to avoid multiple error fragments being shown
            android.app.FragmentManager fm = getFragmentManager();
            fm.executePendingTransactions();

            android.app.DialogFragment existingFragment = (android.app.DialogFragment) fm
                    .findFragmentByTag(TAG_ERROR_DIALOG);
            if (existingFragment != null) {
                // Just show the latest error
                existingFragment.dismiss();
            }

            android.app.DialogFragment errorFragment = (android.app.DialogFragment) factory.prepareErrorFragment(event,
                    finishAfterDialog, argumentsForErrorDialog);
            if (errorFragment != null) {
                errorFragment.show(fm, TAG_ERROR_DIALOG);
            }
        }

        public static void attachTo(Activity activity, Object executionScope, boolean finishAfterDialog, Bundle argumentsForErrorDialog) {
            android.app.FragmentManager fm = activity.getFragmentManager();
            HoneycombManagerFragment fragment = (HoneycombManagerFragment) fm
                    .findFragmentByTag(TAG_ERROR_DIALOG_MANAGER);
            if (fragment == null) {
                fragment = new HoneycombManagerFragment();
                fm.beginTransaction().add(fragment, TAG_ERROR_DIALOG_MANAGER).commit();
                fm.executePendingTransactions();
            }
            fragment.finishAfterDialog = finishAfterDialog;
            fragment.argumentsForErrorDialog = argumentsForErrorDialog;
            fragment.executionScope = executionScope;
        }
    }

    /** Must be set by the application. */
    public static ErrorDialogFragmentFactory<?> factory;

    protected static final String TAG_ERROR_DIALOG = "de.greenrobot.eventbus.error_dialog";
    protected static final String TAG_ERROR_DIALOG_MANAGER = "de.greenrobot.eventbus.error_dialog_manager";

    public static final String KEY_TITLE = "de.greenrobot.eventbus.errordialog.title";
    public static final String KEY_MESSAGE = "de.greenrobot.eventbus.errordialog.message";
    public static final String KEY_FINISH_AFTER_DIALOG = "de.greenrobot.eventbus.errordialog.finish_after_dialog";
    public static final String KEY_ICON_ID = "de.greenrobot.eventbus.errordialog.icon_id";
    public static final String KEY_EVENT_TYPE_ON_CLOSE = "de.greenrobot.eventbus.errordialog.event_type_on_close";

    /** Scope is limited to the activity's class. */
    public static void attachTo(Activity activity) {
        attachTo(activity, false, null);
    }

    /** Scope is limited to the activity's class. */
    public static void attachTo(Activity activity, boolean finishAfterDialog) {
        attachTo(activity, finishAfterDialog, null);
    }

    /** Scope is limited to the activity's class. */
    public static void attachTo(Activity activity, boolean finishAfterDialog, Bundle argumentsForErrorDialog) {
        Object executionScope = activity.getClass();
        attachTo(activity, executionScope, finishAfterDialog, argumentsForErrorDialog);
    }
    
    public static void attachTo(Activity activity, Object executionScope, boolean finishAfterDialog, Bundle argumentsForErrorDialog) {
        if (factory == null) {
            throw new RuntimeException("You must set the static factory field to configure error dialogs for your app.");
        }
        if (isSupportActivity(activity)) {
            SupportManagerFragment.attachTo(activity, executionScope, finishAfterDialog, argumentsForErrorDialog);
        } else {
            HoneycombManagerFragment.attachTo(activity, executionScope, finishAfterDialog, argumentsForErrorDialog);
        }
    }

    private static boolean isSupportActivity(Activity activity) {
        boolean isSupport = false;
        for (Class<?> c = activity.getClass().getSuperclass();; c = c.getSuperclass()) {
            if (c == null) {
                throw new RuntimeException("Illegal activity type: " + activity.getClass());
            }
            String name = c.getName();
            if (name.equals("android.support.v4.app.FragmentActivity")) {
                isSupport = true;
                break;
            } else if (name.startsWith("com.actionbarsherlock.app")
                    && (name.endsWith(".SherlockActivity") || name.endsWith(".SherlockListActivity") || name
                            .endsWith(".SherlockPreferenceActivity"))) {
                throw new RuntimeException("Please use SherlockFragmentActivity. Illegal activity: " + name);
            } else if (name.equals("android.app.Activity")) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    throw new RuntimeException(
                            "Illegal activity without fragment support. Either use Android 3.0+ or android.support.v4.app.FragmentActivity.");
                }
                break;
            }
        }
        return isSupport;
    }

    protected static void checkLogException(ThrowableFailureEvent event) {
        if (factory.config.logExceptions) {
            String tag = factory.config.tagForLoggingExceptions;
            if (tag == null) {
                tag = EventBus.TAG;
            }
            Log.i(tag, "Error dialog manager received exception", event.throwable);
        }
    }

    private static boolean isInExecutionScope(Object executionScope, ThrowableFailureEvent event) {
        if (event != null) {
            Object eventExecutionScope = event.getExecutionScope();
            if (eventExecutionScope != null && !eventExecutionScope.equals(executionScope)) {
                // Event not in our scope, do nothing
                return false;
            }
        }
        return true;
    }

}
