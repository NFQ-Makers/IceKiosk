package com.nfq.hh.icekiosk.app;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class FileCache {

    private File cacheDir;
    private Context context;

    public FileCache(Context context){

        this.context = context;

        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "IceKiosk");
        }
        else {
            cacheDir = this.context.getCacheDir();
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public File getFile(String url) {
        String filename = String.valueOf(url.hashCode());

        File file;
        file = new File(cacheDir, filename);

        if (!file.isFile()) {

            HttpClient httpclient = new NfqHttpClient(context);
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response;

            try {
                // http way
                // InputStream is = new URL(url).openStream();

                // people ssl way
                response = httpclient.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();

                OutputStream os = new FileOutputStream(file);
                copyStream(is, os);
                os.close();
            } catch (Exception e) {
                Log.d("", e.toString());
            }
        }
        return file;
    }

    public void clear() {
        File[] files=cacheDir.listFiles();

        if (files==null) {
            return;
        }

        for (File f:files) {
            f.delete();
        }
    }

    private static void copyStream(InputStream is, OutputStream os) {
        final int buffer_size=1024;
        try {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch (Exception e) {
            Log.d("", e.toString());
        }
    }
}


