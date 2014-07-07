package com.nfq.hh.icekiosk.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class UserActivity extends BaseActivity implements View.OnClickListener {

    private TextView textQty, tvPortionCount;
    private Integer portionCount;
    private String userId;
    private ImageButton plusButton, minusButton, cancelButton, primaryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.LinearLayout);
        linearLayout.requestFocus();

        portionCount = getIntent().getIntExtra("portionCount", 1);
        userId = getIntent().getStringExtra("userId");

        if (isOnline()) {
            new LoadDataTask().execute();
        }
        else {
            finish();
        }

        textQty = (TextView) findViewById(R.id.textQty);
        textQty.setTypeface(tfItalic);

        tvPortionCount = (TextView) findViewById(R.id.portionCount);
        tvPortionCount.setTypeface(tfBold);
        tvPortionCount.setText(String.valueOf(portionCount));

        cancelButton = (ImageButton) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

        primaryButton = (ImageButton) findViewById(R.id.primaryButton);
        primaryButton.setOnClickListener(this);

        minusButton = (ImageButton) findViewById(R.id.minusButton);
        minusButton.setOnClickListener(this);

        plusButton = (ImageButton) findViewById(R.id.plusButton);
        plusButton.setOnClickListener(this);

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
            case R.id.cancelButton:
                Tracker t = GoogleAnalytics.getInstance(this).newTracker(IceKioskApplication.PROPERTY_ID);
                t.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("User Sign Out").setLabel(userId).build());

                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.setAction("action" + System.currentTimeMillis());
                startActivity(i);
                break;
            case R.id.primaryButton:
                if (isOnline()) {
                    new SendDataTask().execute();
                }
                break;
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

    private class LoadDataTask extends AsyncTask<Void, UserData, UserData> {

        @Override
        protected UserData doInBackground(Void... params) {
            try {
                String url = API_URL_USERINFOBYRFID;
                url = url.replace("%d", userId);
                URL u = new URL(url);

                URLConnection tc = u.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(tc.getInputStream()));

                try {
                    JSONObject jo = new JSONObject(in.readLine());
                    UserData d = new UserData();
                    d.setId(jo.getJSONObject("user").getString("id"));
                    d.setUserName(jo.getJSONObject("user").getString("firstName"));
                    d.setUserNotes(jo.getJSONObject("info").getString("text"));
                    d.setUserImageUrl(jo.getJSONObject("user").getString("img"));
                    return d;
                } catch (JSONException e) {
                    Log.d("", e.toString());
                }
            } catch (NullPointerException e ){
                Log.d("", e.toString());
            } catch (Exception e) {
                Log.d("", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(UserData d) {
            if (d != null) {
                TextView userName = (TextView) findViewById(R.id.userName);
                userName.setText(d.getUserName());

                TextView userNotes = (TextView) findViewById(R.id.userNotes);
                userNotes.setText(Html.fromHtml(d.getUserNotes()));

                new DownloadImageTask((ImageView) findViewById(R.id.imageUser)).execute(d.getUserImageUrl());
            } else {
                Toast.makeText(getApplicationContext(), R.string.smth_wrong, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap mBitmap = null;

            try {
                File file = new FileCache(getBaseContext()).getFile(url);
                mBitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            }
            catch (FileNotFoundException e) {
                Log.d("", e.toString());
            }

            return mBitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if(result != null) {
                bmImage.setImageBitmap(result);
            }
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

                // [{"time":{"sec":1398619851,"usec":844563},"deviceId":321,"type":"IceCream", "data":{"userId":0,"amount":3}}]
                JSONArray jsonParams = new JSONArray();
                jsonParams.put(
                        new JSONObject()
                                .put("deviceId", API_DEVICEID)
                                .put("type", "IceScream")
                                .put("time", new JSONObject().put("sec", System.currentTimeMillis() / 1000).put("usec", 0))
                                .put("data", new JSONObject().put("userId", userId).put("amount", portionCount))
                );

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
                    Log.d("", e.toString());
                    return false;
                }
            } catch (Exception e) {
                Log.d("", e.toString());
                return false;
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Tracker t = GoogleAnalytics.getInstance(getBaseContext()).newTracker(IceKioskApplication.PROPERTY_ID);
                t.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("User Buy").setLabel(userId).setValue(portionCount).build());

                String transactioId = String.valueOf(System.currentTimeMillis() / 1000);
                Tracker ecommerceTracker = ((IceKioskApplication) getApplication()).getTracker(IceKioskApplication.TrackerName.ECOMMERCE_TRACKER);
                ecommerceTracker.send(
                        new HitBuilders.TransactionBuilder()
                                .setTransactionId(transactioId)
                                .setAffiliation(API_DEVICEID)
                                .setRevenue(portionCount)
                                .setTax(portionCount * 0.21)
                                .setShipping(0)
                                .build()
                );
                ecommerceTracker.send(
                        new HitBuilders.ItemBuilder()
                                .setTransactionId(transactioId)
                                .setName("IceScream")
                                .setSku("1")
                                .setPrice(1)
                                .setQuantity(portionCount)
                                .build()
                );

                Intent i = new Intent(getApplicationContext(), ThankYouActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.setAction("action" + System.currentTimeMillis());
                startActivity(i);
            } else {
                Toast.makeText(getApplicationContext(), R.string.smth_wrong, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
