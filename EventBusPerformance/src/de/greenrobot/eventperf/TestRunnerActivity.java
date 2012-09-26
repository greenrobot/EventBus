package de.greenrobot.eventperf;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;
import de.greenrobot.event.EventBus;

/**
 * This activity gets the information from the activity before, sets up the test and starts the test. After it watchs
 * after that, if a test is finished. When a test is finished, the activity appends it on the textview analyse. If all
 * test are finished, it cancels the timer.
 */
public class TestRunnerActivity extends Activity {

    private TestRunner testRunner;
    private EventBus controlBus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runtests);
        controlBus = new EventBus();
        controlBus.register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (testRunner == null) {
            TestParams testParams = (TestParams) getIntent().getSerializableExtra("params");
            testRunner = new TestRunner(getApplicationContext(), testParams, controlBus);
            testRunner.start();
        }
    }

    public void onEventMainThread(TestFinishedEvent event) {
        TextView textView = (TextView) findViewById(R.id.textViewResult);
        Test test = event.test;
        String text = "<b>" + test.getDisplayName() + "</b><br/>" + //
                test.getPrimaryResultMillis() + " ms<br/>" + //
                ((int) test.getPrimaryResultRate()) + " per second<br/>";
        if (test.getOtherTestResults() != null) {
            text += test.getOtherTestResults();
        }
        text += "<br/>----------------<br/>";
        textView.append(Html.fromHtml(text));
        if (event.isLastEvent) {
            findViewById(R.id.buttonCancel).setVisibility(View.GONE);
            findViewById(R.id.textViewTestRunning).setVisibility(View.GONE);
        }
    }

    public void onClickCancel(View view) {
        // Cancel asap
        if (testRunner != null) {
            testRunner.cancel();
            testRunner = null;
        }
        finish();
    }

    public void onDestroy() {
        if (testRunner != null) {
            testRunner.cancel();
        }
        controlBus.unregister(this);
        super.onDestroy();
    }
}
