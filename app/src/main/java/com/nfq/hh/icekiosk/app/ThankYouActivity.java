package com.nfq.hh.icekiosk.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;

public class ThankYouActivity extends BaseActivity implements View.OnClickListener {

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.LinearLayout);
        linearLayout.requestFocus();

        TextView tv1 = (TextView) findViewById(R.id.textView1);
        tv1.setTypeface(tfChaparralProBold);

        TextView tv2 = (TextView) findViewById(R.id.textView2);
        tv2.setTypeface(tfChaparralProRegular);

        ImageView imageSuccess = (ImageView) findViewById(R.id.imageSuccess);
        imageSuccess.setOnClickListener(this);

        ImageButton buttonMore = (ImageButton) findViewById(R.id.buttonMore);
        buttonMore.setOnClickListener(this);

        timer = new CountDownTimer(5000, 1000) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                startMainActivity();
            }
        }.start();

        //Get a Tracker (should auto-report)
        ((IceKioskApplication) getApplication()).getTracker(IceKioskApplication.TrackerName.APP_TRACKER);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public void onClick(View view) {
        startMainActivity();
    }

    protected void startMainActivity() {
        timer.cancel();

        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.setAction("action" + System.currentTimeMillis());
        startActivity(i);
    }
}