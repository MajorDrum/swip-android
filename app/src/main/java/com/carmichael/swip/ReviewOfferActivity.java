package com.carmichael.swip;

import android.app.ActionBar;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.carmichael.swip.Models.TradeItem;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.content.ContentValues.TAG;

public class ReviewOfferActivity extends Activity {

    private TextView tvTitle;
    private TextView tvItemName;
    private TextView tvItemDescription;
    private ImageView imgMyItem;
    private Button btnAcceptOffer;
    private TradeItem myItem;
    private TradeItem theirItem;
    boolean myItemReady = false;
    boolean theirItemReady = false;
    private String myItemKey;
    private String theirItemKey;
    private DatabaseReference myItemRef;
    private DatabaseReference theirItemRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_offer);

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

        myItemKey = getIntent().getStringExtra("MyItemKey");
        theirItemKey = getIntent().getStringExtra("TheirItemKey");

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        myItemRef = mDatabase.child("TradeItems").child(myItemKey);
        theirItemRef = mDatabase.child("TradeItems").child(theirItemKey);

        myItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myItem = dataSnapshot.getValue(TradeItem.class);
                myItem.setItemId(dataSnapshot.getKey());
                myItemReady = true;
                if(myItemReady && theirItemReady){
                    BeginActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        theirItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                theirItem = dataSnapshot.getValue(TradeItem.class);
                theirItem.setItemId(dataSnapshot.getKey());
                theirItemReady = true;
                if(myItemReady && theirItemReady){
                    BeginActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void BeginActivity(){

        tvTitle.setText(Html.fromHtml("<b>"+theirItem.getName()+"</b>" + " for " + "<b>"+myItem.getName()+"</b>"));
        tvItemName.setText(theirItem.getName());
        tvItemDescription.setText(theirItem.getDescription());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String location = "TradeItems/" + theirItem.getItemId();
        StorageReference ref = storageRef.child(location);

        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(ref)
                .into(imgMyItem);

        tvTitle.setVisibility(View.VISIBLE);
        tvItemName.setVisibility(View.VISIBLE);
        tvItemDescription.setVisibility(View.VISIBLE);
        imgMyItem.setVisibility(View.VISIBLE);
        btnAcceptOffer.setVisibility(View.VISIBLE);

//        if(theirItem.getHashMapKeysAsStrings(theirItem.getMatches()).contains(myItem.getItemId())){
//            btnAcceptOffer.setText("NULL");
//        }else{


//            btnAcceptOffer.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    myItemRef.child("offers").child(theirItemKey).removeValue();
//                    if(theirItem.getHashMapKeysAsStrings(theirItem.getPending()).contains(myItemKey)){
//                        myItemRef.child("matches").child(theirItemKey).setValue("true");
//                        theirItemRef.child("matches").child(myItemKey).setValue("true");
//                        theirItemRef.child("pending").child(myItemKey).removeValue();
//                    }else{
//                        myItemRef.child("pending").child(theirItemKey).setValue("true");
//                        theirItemRef.child("matches").child(myItemKey).setValue("true");
//                    }
//                    onBackPressed();
//                }
//            });


//        }

    }
}
