package com.nfq.hh.icekiosk.app;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class FileCache {

    private File cacheDir;

    public FileCache(Context context){
        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "IceKiosk");
        }
        else {
            cacheDir = context.getCacheDir();
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public File getFile(String url) {
        String filename=String.valueOf(url.hashCode());
        // String filename = URLEncoder.encode(url);
        File file;

        file = new File(cacheDir, filename);

        if (!file.isFile()) {
            try {
                InputStream is = new URL(url).openStream();
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


