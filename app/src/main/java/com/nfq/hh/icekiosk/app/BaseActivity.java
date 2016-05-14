package com.nfq.hh.icekiosk.app;

import android.app.Activity;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

public class BaseActivity extends Activity implements View.OnKeyListener {

    protected Typeface tfChaparralProRegular, tfChaparralProBold, tfChaparralProItalic;
    protected Typeface tfSourceSansProRegular, tfSourceSansProBold, tfSourceSansProItalic, tfSourceSansProLight;
    protected Integer minPortionCount = 1, maxPortionCount = 9;

    // configurable values
    protected final String API_DEVICEID = "IceKiosk_1";
    protected final double ICE_PRICE = 0.25;
    protected final String API_URL_USERDATA = "";
    protected final String API_URL_USERINFO = "";
    protected final String API_URL_EVENT = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tfChaparralProRegular = Typeface.createFromAsset(getAssets(), "fonts/ChaparralPro_Regular.otf");
        tfChaparralProBold = Typeface.createFromAsset(getAssets(), "fonts/ChaparralPro_Bold.otf");
        tfChaparralProItalic = Typeface.createFromAsset(getAssets(), "fonts/ChaparralPro_Italic.otf");

        tfSourceSansProRegular = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Regular.ttf");
        tfSourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Bold.ttf");
        tfSourceSansProItalic = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Italic.ttf");
        tfSourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getBaseContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        return false;
    }
}
