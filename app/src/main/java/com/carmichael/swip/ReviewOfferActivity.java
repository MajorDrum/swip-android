package com.carmichael.swip;

import android.app.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.carmichael.swip.Contracts.APIContract;
import com.carmichael.swip.Models.TradeItem;
import com.carmichael.swip.Models.User;
import com.carmichael.swip.Services.FirebaseServices;
import com.carmichael.swip.Services.ImageServices;
import com.carmichael.swip.Services.WebServices;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import static android.content.ContentValues.TAG;

public class ReviewOfferActivity extends Activity {

    private TextView tvTitle;
    private TextView tvItemName;
    private TextView tvItemDescription;
    private ImageView imgMyItem;
    private Button btnAcceptOffer;
    private TradeItem myItem;
    private TradeItem theirItem;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_offer);

        initView();

        String myItemKey = getIntent().getStringExtra("MyItemKey");
        String theirItemKey = getIntent().getStringExtra("TheirItemKey");
        user = getIntent().getParcelableExtra("User");

        String myJson = null;
        String theirJson = null;

        try{
            myJson = new RetrieveJsonTask().execute(APIContract.URL_DATABASE_TRADEITEMS+myItemKey,user.getToken()).get();
            theirJson = new RetrieveJsonTask().execute(APIContract.URL_DATABASE_TRADEITEMS+theirItemKey,user.getToken()).get();
        }catch(Exception e){
            Log.e(TAG, "onCreate: unable to retrieve json", e);
        }

        myItem = FirebaseServices.convertJsonToTradeItem(myJson, myItemKey);
        theirItem = FirebaseServices.convertJsonToTradeItem(theirJson,theirItemKey);

        Log.d(TAG, "onCreate: item test 1: " + myItem.toString());
        Log.d(TAG, "onCreate: item test 2: " + myItem.toString());

        beginActivity();
    }

    public void initView(){
        this.setFinishOnTouchOutside(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvItemName = (TextView) findViewById(R.id.tvItemName);
        tvItemDescription = (TextView) findViewById(R.id.tvItemDescription);
        imgMyItem = (ImageView) findViewById(R.id.imgMyItem);
        btnAcceptOffer = (Button) findViewById(R.id.btnAcceptOffer);
        tvTitle.setVisibility(View.INVISIBLE);
        tvItemName.setVisibility(View.INVISIBLE);
        tvItemDescription.setVisibility(View.INVISIBLE);
        imgMyItem.setVisibility(View.INVISIBLE);
        btnAcceptOffer.setVisibility(View.INVISIBLE);
    }

    public void beginActivity(){
        tvTitle.setText(Html.fromHtml("<b>"+theirItem.getName()+"</b>" + " for " + "<b>"+myItem.getName()+"</b>"));
        tvItemName.setText(theirItem.getName());
        tvItemDescription.setText(theirItem.getDescription());
        
        String location = "TradeItems/" + theirItem.getItemId();
        StorageReference ref = FirebaseServices.getStorageReference(location);

        ImageServices.setImageWithGlide(this,ref,imgMyItem);

        tvTitle.setVisibility(View.VISIBLE);
        tvItemName.setVisibility(View.VISIBLE);
        tvItemDescription.setVisibility(View.VISIBLE);
        imgMyItem.setVisibility(View.VISIBLE);
        btnAcceptOffer.setVisibility(View.VISIBLE);
    }

    private class RetrieveJsonTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String json = WebServices.getFirebaseJson(params[0],params[1]);
            return json;
        }
    }
}
