package com.carmichael.swip.Services;


import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.http.HttpResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by carte on 2/9/2018.
 */

public class WebServices {

    public static String getFirebaseJson(String urlSnippet, String token){
        String json = null;
        try{
            urlSnippet = urlSnippet + ".json?auth=" + token;
            HttpURLConnection urlConnection = null;
            URL url = new URL(urlSnippet);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */ );
            urlConnection.setConnectTimeout(15000 /* milliseconds */ );
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            json = sb.toString();


        }catch(Exception e){
            Log.e(TAG, "getJson: could not retrieve json", e);
        }
        return json;
    }
}
