package com.carmichael.swip.Services;

import android.os.AsyncTask;

/**
 * Created by carte on 2/10/2018.
 */

public class RetrieveJsonTask extends AsyncTask<String, String, String> {
    @Override
    protected String doInBackground(String... params) {
        String json = WebServices.getFirebaseJson(params[0],params[1]);
        return json;
    }
}
