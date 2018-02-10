package com.carmichael.swip.Services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;

/**
 * Created by carte on 7/27/2017.
 */

public class BitmapRetriever extends AsyncTask<String, String, Bitmap> {
    private static final String TAG = "JsonRetriever";
    Bitmap bmp = null;
    @Override
    protected Bitmap doInBackground(String... params) {
        try{
            URL url = new URL(params[0]);
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        }catch(Exception e){
            Log.e(TAG, "doInBackground: error is: " + e.getStackTrace() );
        }

        return bmp;
    }

    @Override
    protected void onPostExecute(Bitmap b) {
        super.onPostExecute(b);
    }
}