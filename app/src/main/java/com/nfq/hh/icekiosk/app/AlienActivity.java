package com.nfq.hh.icekiosk.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;


public class AlienActivity extends BaseActivity implements View.OnClickListener {

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alien);

        TextView tv3 = (TextView) findViewById(R.id.textView3);
        tv3.setTypeface(tfChaparralProRegular);

        ImageView imageSad = (ImageView) findViewById(R.id.imageSad);
        imageSad.setOnClickListener(this);

        ImageView backButton = (ImageView) findViewById(R.id.alienBackButton);
        backButton.setOnClickListener(this);


        String userId = getIntent().getStringExtra("userId");
        String text = getResources().getString(R.string.smth_wrong).replace("%s", userId);
        tv3.setText(Html.fromHtml(text));

        timer = new CountDownTimer(10000, 1000) {

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
