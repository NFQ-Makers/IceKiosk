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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UserActivity extends BaseActivity implements View.OnClickListener {

    private TextView textQty, tvPortionCount;
    private Integer portionCount;
    private String userId;
    private ImageButton plusButton, minusButton, cancelButton, primaryButton, payButton;
    private UserData d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.LinearLayout);
        linearLayout.requestFocus();

        portionCount = getIntent().getIntExtra("portionCount", 1);
        userId = getIntent().getStringExtra("userId");

        if (isOnline()) {
            new LoadUserDataTask().execute();
        }
        else {
            finish();
        }

        textQty = (TextView) findViewById(R.id.textQty);
        textQty.setTypeface(tfChaparralProItalic);

        tvPortionCount = (TextView) findViewById(R.id.portionCount);
        tvPortionCount.setTypeface(tfChaparralProBold);
        tvPortionCount.setText(String.valueOf(portionCount));

        cancelButton = (ImageButton) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

        primaryButton = (ImageButton) findViewById(R.id.primaryButton);
        primaryButton.setOnClickListener(this);

        minusButton = (ImageButton) findViewById(R.id.minusButton);
        minusButton.setOnClickListener(this);

        plusButton = (ImageButton) findViewById(R.id.plusButton);
        plusButton.setOnClickListener(this);

        payButton = (ImageButton) findViewById(R.id.payButton);
        payButton.setOnClickListener(this);
        payButton.setClickable(false);

        //Get a Tracker (should auto-report)
        ((IceKioskApplication) getApplication()).getTracker(IceKioskApplication.TrackerName.APP_TRACKER);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
        //GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Stop the analytics tracking
        //GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancelButton:
                //Tracker t = GoogleAnalytics.getInstance(this).newTracker(IceKioskApplication.PROPERTY_ID);
                //t.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("User Sign Out").setLabel(userId).build());

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
            case R.id.payButton:
                if (isOnline()) {
                    Intent iPay = new Intent(getApplicationContext(), PayActivity.class);
                    iPay.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    iPay.setAction("action" + System.currentTimeMillis());
                    iPay.putExtra("userId", userId);
                    iPay.putExtra("totalAmount", d.getTotalAmount());
                    iPay.putExtra("totalPaid", d.getTotalPaid());
                    iPay.putExtra("portionCount", portionCount);
                    startActivity(iPay);
                }
            default:
                break;
        }
    }

    private class LoadUserDataTask extends AsyncTask<Void, UserData, UserData> {

        @Override
        protected UserData doInBackground(Void... params) {
            try {
                String url = API_URL_USERDATA;
                url = url.replace("%d", userId);
                URL u = new URL(url);

                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                httpGet.setHeader("x-api-token", API_TOKEN);
                HttpResponse response;

                try {
                    response = httpclient.execute(httpGet);

                    HttpEntity entity = response.getEntity();
                    BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));

                    JSONObject jo = new JSONObject(in.readLine());

                    JSONObject person = jo.getJSONObject("data");

                    UserData d = new UserData();

                    d.setIntranetId(person.getInt("id"));
                    d.setUserName(person.getString("first_name").trim());
                    d.setUserImageUrl(IMAGE_URL_HOST + person.getString("avatar"));

                    JSONArray cards = person.getJSONArray("cards");

                    String card = null;
                    for (int i = 0; i < cards.length(); i++) {
                        if (cards.get(i).equals(userId)) {
                            card = (String)cards.get(i);
                            break;
                        }
                    }

                    if (card == null) {
                        return null;
                    }

                    userId = card;
                    d.setIdCard(userId);

                    d.setTotalAmount(person.getInt("ice_cream_taken"));
                    d.setTotalPaid(person.getInt("ice_cream_paid"));

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
                userName.setTypeface(tfSourceSansProBold);

                TextView tvTotalAmount = (TextView) findViewById(R.id.totalAmount);
                tvTotalAmount.setTypeface(tfSourceSansProRegular);
                tvTotalAmount.setText("Suvalgei: " + String.valueOf(d.getTotalAmount()));

                TextView tvTotalPaid = (TextView) findViewById(R.id.totalPaid);
                tvTotalPaid.setTypeface(tfSourceSansProRegular);
                tvTotalPaid.setText("Apmokėjai: " + String.valueOf(d.getTotalPaid()));

                if (d.getTotalAmount() > d.getTotalPaid()) {
                    payButton.setAlpha((float)1);
                    payButton.setClickable(true);
                }

                UserActivity.this.d = d;

                new LoadUserInfoTask().execute(d);
                new DownloadImageTask((ImageView) findViewById(R.id.imageUser)).execute(d.getUserImageUrl());
            } else {
                // smth is wrong show sad face :(
                Intent i = new Intent(getApplicationContext(), AlienActivity.class);

                i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.setAction("action" + System.currentTimeMillis());
                i.putExtra("userId", userId);

                startActivity(i);
            }
        }
    }

    private class LoadUserInfoTask extends AsyncTask<UserData, UserData, UserData> {
        @Override
        protected UserData doInBackground(UserData... userDatas) {
            UserData d = userDatas[0];
            d.setUserNotes("");

            try {
                String url = API_URL_JOKE;
                url = url.replace("%s", d.getUserName());
                URL u = new URL(url);

                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response;

                try {
                    response = httpclient.execute(httpGet);

                    HttpEntity entity = response.getEntity();
                    BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
                    JSONObject jo = new JSONObject(in.readLine());

                    String joke = jo.getJSONObject("value")
                            .getString("joke")
                            .replaceAll(" +", " ");

                    d.setUserNotes(joke);

                    return d;
                } catch (JSONException e) {
                    Log.d("", e.toString());
                }
            } catch (NullPointerException e ){
                Log.d("", e.toString());
            } catch (Exception e) {
                Log.d("", e.toString());
            }

            return d;
        }

        protected void onPostExecute(UserData d) {
            TextView userNotes = (TextView) findViewById(R.id.userNotes);

            userNotes.setText(Html.fromHtml(d.getUserNotes()));
            userNotes.setTypeface(tfSourceSansProRegular);

            UserActivity.this.d = d;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected int calculateInSampleSize(
                BitmapFactory.Options options,
                int reqWidth,
                int reqHeight
        ) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap mBitmap = null;

            try {

                BitmapFactory.Options options = new BitmapFactory.Options();
                File file = new FileCache(getBaseContext()).getFile(url);

                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(new FileInputStream(file), null, options);

                options.inSampleSize = calculateInSampleSize(options, 250, 250);
                options.inJustDecodeBounds = false;

                mBitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
            }
            catch (OutOfMemoryError e) {
                Log.d("", "No ram mate");
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
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(API_URL_EVENT);
            post.setHeader("x-api-token", API_TOKEN);

            List<NameValuePair> postData = new ArrayList<NameValuePair>(3);
            postData.add(new BasicNameValuePair("type", "IceCream"));
            postData.add(new BasicNameValuePair("amount", portionCount.toString()));
            postData.add(new BasicNameValuePair("card_number", d.getIdCard()));

            try {
                post.setEntity(new UrlEncodedFormEntity(postData));
                HttpResponse response = client.execute(post);

                return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
            } catch (ClientProtocolException e) {
                Log.d("", e.toString());
            } catch (IOException e) {
                Log.d("", e.toString());
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
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
