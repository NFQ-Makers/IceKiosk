package com.nfq.hh.icekiosk.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * min 1
 * x/5*3
 * x/5*2
 * x/5*1
 * max x
 */
public class PayActivity extends BaseActivity implements View.OnClickListener {

    private String userId;
    private Integer portionCount;
    private TextView textView1, textView2, textView3, textView4, textView5, textView6, textView7, textView8, textView9, textView10;
    private double payAmount, unPaidSum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        Integer totalAmount = getIntent().getIntExtra("totalAmount", 0);
        Integer totalPaid = getIntent().getIntExtra("totalPaid", 0);
        portionCount = getIntent().getIntExtra("portionCount", 0);
        userId = getIntent().getStringExtra("userId");
        payAmount = unPaidSum = (totalAmount - totalPaid) * ICE_PRICE;
        double x1, x2, x3, x4, x5;

        x1 = 1 * ICE_PRICE;
        x2 = (totalAmount - totalPaid) / 5 * 1 * ICE_PRICE;
        x3 = (totalAmount - totalPaid) / 5 * 2 * ICE_PRICE;
        if (x3 < x1) {
            x3 = x1;
        }
        x4 = (totalAmount - totalPaid) / 5 * 3 * ICE_PRICE;
        x5 = unPaidSum;

        textView1 = (TextView) findViewById(R.id.textView1);
        textView1.setTypeface(tfChaparralProRegular);
        textView1.setText(Html.fromHtml(textView1.getText() + "<b>" + String.format("%.2f", unPaidSum) + "</b>"));

        textView2 = (TextView) findViewById(R.id.textView2);
        textView2.setTypeface(tfSourceSansProLight);

        textView3 = (TextView) findViewById(R.id.textView3);
        textView3.setTypeface(tfSourceSansProLight);

        textView4 = (TextView) findViewById(R.id.textView4);
        textView4.setTypeface(tfChaparralProRegular);

        textView5 = (TextView) findViewById(R.id.textView5);
        textView5.setTypeface(tfChaparralProBold);
        textView5.setText(String.format("%.2f", unPaidSum));

        textView6 = (TextView) findViewById(R.id.textView6);
        textView6.setTypeface(tfSourceSansProBold);
        textView6.setOnClickListener(this);
        textView6.setText(String.format("%.2f", x1));
        if (x3 - x1 > 2.25) {
            textView6.setText(String.format("%.2f", 1.00));
        }

        textView7 = (TextView) findViewById(R.id.textView7);
        textView7.setTypeface(tfSourceSansProBold);
        textView7.setOnClickListener(this);
        if (x2 < x1) {
            x2 = x1;
        }
        textView7.setText(String.format("%.2f", x2));
        if (x2 > x1 && x3 > 1) {
            textView7.setText(String.format("%.0f", x2) + ".00");
        }

        textView8 = (TextView) findViewById(R.id.textView8);
        textView8.setTypeface(tfSourceSansProBold);
        textView8.setOnClickListener(this);
        textView8.setText(String.format("%.2f", x3));

        textView9 = (TextView) findViewById(R.id.textView9);
        textView9.setTypeface(tfSourceSansProBold);
        textView9.setOnClickListener(this);
        if (x4 < x1) {
            x4 = x1;
            textView9.setText(String.format("%.2f", x4));
        }
        if (x4 == x3 && x4 < x5-x3) {
            textView9.setText(String.format("%.2f", x4 + ICE_PRICE));
        }
        else if (x4 > x3) {
            textView9.setText(String.format("%.0f", x4) + ".00");
        }

        textView10 = (TextView) findViewById(R.id.textView10);
        textView10.setTypeface(tfSourceSansProBold);
        textView10.setOnClickListener(this);
        textView10.setText(String.format("%.2f", x5));

        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        ImageButton payButton = (ImageButton) findViewById(R.id.payButton);
        payButton.setOnClickListener(this);

        ImageButton payPlusButton = (ImageButton) findViewById(R.id.payPlusButton);
        payPlusButton.setOnClickListener(this);

        ImageButton payMinusButton = (ImageButton) findViewById(R.id.payMinusButton);
        payMinusButton.setOnClickListener(this);

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
        switch (view.getId()) {
            case R.id.backButton:
                if (isOnline()) {
                    Intent i = new Intent(getApplicationContext(), UserActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    i.putExtra("portionCount", portionCount);
                    i.putExtra("userId", userId);
                    i.setAction("action" + System.currentTimeMillis());
                    startActivity(i);
                }
                break;
            case R.id.payPlusButton:
                payAmount = payAmount + ICE_PRICE;
                if (payAmount > unPaidSum) {
                    payAmount = unPaidSum;
                }
                textView5.setText(String.format("%.2f", payAmount));
                break;
            case R.id.payMinusButton:
                payAmount = payAmount - ICE_PRICE;
                if (payAmount < ICE_PRICE) {
                    payAmount = ICE_PRICE;
                }
                textView5.setText(String.format("%.2f", payAmount));
                break;
            case R.id.textView6:
            case R.id.textView7:
            case R.id.textView8:
            case R.id.textView9:
            case R.id.textView10:
                TextView tv = (TextView) findViewById(view.getId());
                textView5.setText(tv.getText().toString());
                payAmount = Double.parseDouble(tv.getText().toString());
                break;
            case R.id.payButton:
                if (isOnline()) {
                    new SendDataTask().execute();
                }
                break;
            default:
                break;
        }
    }

    private class SendDataTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = API_URL_EVENT;
            try {
                URL u = new URL(url);
                HttpURLConnection urlConn = (HttpURLConnection) u.openConnection();
                urlConn.setRequestMethod("POST");
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setDoOutput(true);
                urlConn.setDoInput(true);
                urlConn.connect();

                // [{"time":{"sec":1398619851,"usec":844563},"deviceId":321,"type":"IceCreamPay", "data":{"userId":0,"amount":-1}}]
                JSONArray jsonParams = new JSONArray();
                jsonParams.put(
                        new JSONObject()
                                .put("deviceId", API_DEVICEID)
                                .put("type", "IceCreamPay")
                                .put("time", new JSONObject().put("sec", System.currentTimeMillis() / 1000).put("usec", 0))
                                .put("data", new JSONObject().put("userId", userId).put("amount", payAmount / -ICE_PRICE))
                );
                Log.d("", jsonParams.toString());

                DataOutputStream os = new DataOutputStream(urlConn.getOutputStream());
                os.write(jsonParams.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                try {
                    JSONObject jo = new JSONObject(in.readLine());
                    // {"status":"ok"}
                    if (jo.getString("status").equals("ok")) {
                        return true;
                    }
                } catch (JSONException e) {
//                    Log.d("", e.toString());
                    return false;
                }
            } catch (Exception e) {
//                Log.d("", e.toString());
                return false;
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Tracker t = GoogleAnalytics.getInstance(getBaseContext()).newTracker(IceKioskApplication.PROPERTY_ID);
                t.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("User Pay").setLabel(userId).setValue((long)payAmount).build());

                Intent i = new Intent(getApplicationContext(), PayThankYouActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.setAction("action" + System.currentTimeMillis());
                i.putExtra("paidSum", payAmount);
                i.putExtra("unPaidSum", unPaidSum - payAmount);
                startActivity(i);
            } else {
                Toast.makeText(getApplicationContext(), R.string.smth_wrong, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
