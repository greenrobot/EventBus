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

package org.greenrobot.eventbusperf;

import android.app.Activity;
import android.os.Bundle;
import android.os.Process;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * This activity gets the information from the activity before, sets up the test and starts the test. After it watchs
 * after that, if a test is finished. When a test is finished, the activity appends it on the textview analyse. If all
 * test are finished, it cancels the timer.
 */
public class TestRunnerActivity extends Activity {
    private TestRunner mTestRunner;
    private EventBus mControlBus;
    private TextView mTextViewResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runtests);
        mTextViewResult = (TextView) findViewById(R.id.textViewResult);
        mControlBus = new EventBus();
        mControlBus.register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTestRunner == null) {
            TestParams testParams = (TestParams) getIntent().getSerializableExtra("params");
            mTestRunner = new TestRunner(getApplicationContext(), testParams, mControlBus);

            if (testParams.getTestNumber() == 1) {
                mTextViewResult.append("Events: " + testParams.getEventCount() + "\n");
            }
            mTextViewResult.append("Subscribers: " + testParams.getSubscriberCount() + "\n\n");
            mTestRunner.start();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(TestFinishedEvent event) {
        Test test = event.test;
        String text = "<b>" + test.getDisplayName() + "</b><br/>" + //
                test.getPrimaryResultMicros() + " micro seconds<br/>" + //
                ((int) test.getPrimaryResultRate()) + "/s<br/>";
        if (test.getOtherTestResults() != null) {
            text += test.getOtherTestResults();
        }
        text += "<br/>----------------<br/>";
        mTextViewResult.append(Html.fromHtml(text));
        if (event.isLastEvent) {
            findViewById(R.id.buttonCancel).setVisibility(View.GONE);
            findViewById(R.id.textViewTestRunning).setVisibility(View.GONE);
            findViewById(R.id.buttonKillProcess).setVisibility(View.VISIBLE);
        }
    }

    public void onClickCancel(View view) {
        // Cancel asap
        if (mTestRunner != null) {
            mTestRunner.cancel();
            mTestRunner = null;
        }
        finish();
    }

    public void onClickKillProcess(View view) {
        Process.killProcess(Process.myPid());
    }

    public void onDestroy() {
        if (mTestRunner != null) {
            mTestRunner.cancel();
        }
        mControlBus.unregister(this);
        super.onDestroy();
    }
}
