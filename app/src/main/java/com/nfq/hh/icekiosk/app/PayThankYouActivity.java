package com.nfq.hh.icekiosk.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;


public class PayThankYouActivity extends BaseActivity implements View.OnClickListener {

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_thank_you);

        Double paidSum = getIntent().getDoubleExtra("paidSum", 0);
        Double unPaidSum = getIntent().getDoubleExtra("unPaidSum", 0);

        TextView tyTv = (TextView) findViewById(R.id.tyTextView);
        tyTv.setTypeface(tfChaparralProBold);

        TextView tv2  = (TextView) findViewById(R.id.textView2);
        tv2.setTypeface(tfSourceSansProLight);
        tv2.setText(Html.fromHtml("Sumokėjai <b>" + String.format("%.2f", paidSum) + " €</b> / Liko <b>" + String.format("%.2f", unPaidSum) + " €</b>"));

        TextView tv3 = (TextView) findViewById(R.id.textView3);
        tv3.setTypeface(tfSourceSansProLight);

        ImageView smileImageView = (ImageView) findViewById(R.id.imageView3);
        smileImageView.setOnClickListener(this);

        ImageButton tyBackButton = (ImageButton) findViewById(R.id.tyBackButton);
        tyBackButton.setOnClickListener(this);

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

    protected void startMainActivity() {
        timer.cancel();

        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.setAction("action" + System.currentTimeMillis());
        startActivity(i);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView3:
            case R.id.tyBackButton:
                startMainActivity();
                break;
            default:
                break;
        }
    }
}
