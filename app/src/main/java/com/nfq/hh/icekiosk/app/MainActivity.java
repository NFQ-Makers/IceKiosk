package com.nfq.hh.icekiosk.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    private TextView tv1, tv2, tvPortionCount;
    private ImageButton plusButton, minusButton;
    private Integer portionCount = 1;
    private String keyBoardChars = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.LinearLayout);
        linearLayout.requestFocus();

        tv1 = (TextView) findViewById(R.id.textView1);
        tv1.setTypeface(tfRegular);
        tv2 = (TextView) findViewById(R.id.textView2);
        tv2.setTypeface(tfRegular);
        tvPortionCount = (TextView) findViewById(R.id.portionCount);
        tvPortionCount.setTypeface(tfBold);

        plusButton = (ImageButton) findViewById(R.id.plusButton);
        plusButton.setOnClickListener(this);
        minusButton = (ImageButton) findViewById(R.id.minusButton);
        minusButton.setOnClickListener(this);

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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.plusButton:
                if (portionCount < maxPortionCount) {
                    portionCount++;
                    tvPortionCount.setText(String.valueOf(portionCount));
                }
                break;
            case R.id.minusButton:
                if (portionCount > minPortionCount) {
                    portionCount--;
                    tvPortionCount.setText(String.valueOf(portionCount));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                startUserActivity();
                break;
            case KeyEvent.KEYCODE_0:
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
            case KeyEvent.KEYCODE_9:
            case KeyEvent.KEYCODE_NUMPAD_0:
            case KeyEvent.KEYCODE_NUMPAD_1:
            case KeyEvent.KEYCODE_NUMPAD_2:
            case KeyEvent.KEYCODE_NUMPAD_3:
            case KeyEvent.KEYCODE_NUMPAD_4:
            case KeyEvent.KEYCODE_NUMPAD_5:
            case KeyEvent.KEYCODE_NUMPAD_6:
            case KeyEvent.KEYCODE_NUMPAD_7:
            case KeyEvent.KEYCODE_NUMPAD_8:
            case KeyEvent.KEYCODE_NUMPAD_9:
                keyBoardChars += String.valueOf(event.getNumber());
                break;
        }

        return false;
    }

    public void startUserActivity() {
        if (isOnline()) {
            Tracker t = GoogleAnalytics.getInstance(this).newTracker(IceKioskApplication.PROPERTY_ID);
            t.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("User Sign In").setLabel(keyBoardChars).build());
            t.set("&uid", keyBoardChars);

            Intent i = new Intent(getApplicationContext(), UserActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            i.putExtra("portionCount", portionCount);
            i.putExtra("userId", keyBoardChars);
            i.setAction("action" + System.currentTimeMillis());
            // reset keyboard chars
            keyBoardChars = "";
            startActivity(i);
        }
    }
}
