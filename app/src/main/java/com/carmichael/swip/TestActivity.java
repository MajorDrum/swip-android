package com.carmichael.swip;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.carmichael.swip.Contracts.APIContract;
import com.carmichael.swip.Services.WebServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity";
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser fUser = mAuth.getCurrentUser();

        fUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                token = task.getResult().getToken();
                Log.d(TAG, "onComplete: TASK COMPLETE WITH: " + token);
                try{
                    String json = new TokenTask().execute().get();
                    Log.d(TAG, "onComplete: TestActivity json is: " + json);
                }catch(Exception e){
                    Log.e(TAG, "onComplete: TestActivity could not retrieve JSON",e);
                }


            }
        });
    }

    private class TokenTask extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... params) {
            String json = WebServices.getFirebaseJson(APIContract.URL_DATABASE +"TradeItems/-KtK-BUQCOHt0oZwLf8h",token);
            return json;
        }
    }

}
