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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.greenrobot.eventbus.EventBus;

public class ErrorDialogFragments {
    /** TODO Use config:  Icon res ID to use for all error dialogs. May be configured by each app (optional). */
    public static int ERROR_DIALOG_ICON = 0;

    /** TODO Use config:  Event class to be fired on dismissing the dialog by the user. May be configured by each app. */
    public static Class<?> EVENT_TYPE_ON_CLICK;

    public static Dialog createDialog(Context context, Bundle arguments, OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(arguments.getString(ErrorDialogManager.KEY_TITLE));
        builder.setMessage(arguments.getString(ErrorDialogManager.KEY_MESSAGE));
        if (ERROR_DIALOG_ICON != 0) {
            builder.setIcon(ERROR_DIALOG_ICON);
        }
        builder.setPositiveButton(android.R.string.ok, onClickListener);
        return builder.create();
    }

    public static void handleOnClick(DialogInterface dialog, int which, Activity activity, Bundle arguments) {
        if (EVENT_TYPE_ON_CLICK != null) {
            Object event;
            try {
                event = EVENT_TYPE_ON_CLICK.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Event cannot be constructed", e);
            }
            EventBus eventBus = ErrorDialogManager.factory.config.getEventBus();
            eventBus.post(event);
        }
        boolean finish = arguments.getBoolean(ErrorDialogManager.KEY_FINISH_AFTER_DIALOG, false);
        if (finish && activity != null) {
            activity.finish();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class Honeycomb extends android.app.DialogFragment implements OnClickListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return createDialog(getActivity(), getArguments(), this);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            handleOnClick(dialog, which, getActivity(), getArguments());
        }
    }

    public static class Support extends DialogFragment implements OnClickListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return createDialog(getActivity(), getArguments(), this);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            handleOnClick(dialog, which, getActivity(), getArguments());
        }
    }
}
