package com.nfq.hh.icekiosk.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class IceKioskBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, MainActivity.class);
        context.startService(startServiceIntent);
    }
}
