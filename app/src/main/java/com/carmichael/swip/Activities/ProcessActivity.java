package com.carmichael.swip.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.carmichael.swip.Models.TradeItem;
import com.carmichael.swip.Models.User;
import com.carmichael.swip.R;
import com.carmichael.swip.Services.ImageServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProcessActivity extends AppCompatActivity {

    public static final String TAG = "ProcessActivity";
    private TextView tvTitle;
    private TextView tvName;
    private TextView tvPhoneNumber;
    private TextView tvDescription;
    private ImageView imgTheirItem;
    private Button btnProcessConfirm;
    private String myItemKey;
    private String theirItemKey;
    private FirebaseUser fUser;
    private User user;
    private User otherUser;
    private DatabaseReference mDatabase;
    private DatabaseReference userRef;
    private DatabaseReference otherUserRef;
    private DatabaseReference myItemRef;
    private DatabaseReference theirItemRef;
    private TradeItem myItem;
    private TradeItem theirItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        tvTitle = (TextView) findViewById(R.id.tvProcessTitle);
        tvName = (TextView) findViewById(R.id.tvProcessName);
        tvPhoneNumber = (TextView) findViewById(R.id.tvProcessPhoneNumber);
        tvDescription = (TextView) findViewById(R.id.tvProcessDescription);
        imgTheirItem = (ImageView) findViewById(R.id.imgProcessTheirItem);
        btnProcessConfirm = (Button) findViewById(R.id.btnProcessConfirm);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        myItemKey = getIntent().getStringExtra("MyItemKey");
        theirItemKey = getIntent().getStringExtra("TheirItemKey");

        mDatabase = FirebaseDatabase.getInstance().getReference();

        userRef = mDatabase.child("Users").child(fUser.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                myItemRef = mDatabase.child("TradeItems").child(myItemKey);
                myItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        myItem = dataSnapshot.getValue(TradeItem.class);
                        myItem.setItemId(dataSnapshot.getKey());
                        theirItemRef = mDatabase.child("TradeItems").child(theirItemKey);
                        theirItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                theirItem = dataSnapshot.getValue(TradeItem.class);
                                theirItem.setItemId(dataSnapshot.getKey());
                                otherUserRef = mDatabase.child("Users").child(theirItem.getUserId());
                                otherUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        otherUser = dataSnapshot.getValue(User.class);
                                        otherUser.setUserId(dataSnapshot.getKey());
                                        BeginActivity();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {}
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void BeginActivity(){

        Log.d(TAG, "beginActivity: theirItem is: " + theirItem.toString());
        Log.d(TAG, "beginActivity: myItem is: " + myItem.toString());

        tvTitle.setText(Html.fromHtml("<b>"+theirItem.getName()+"</b>" + " for " + "<b>"+myItem.getName()+"</b>"));
        tvPhoneNumber.setText(otherUser.getPhone());
        tvName.setText(otherUser.getFirstName());
        tvDescription.setText(theirItem.getDescription());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String location = "TradeItems/" + theirItem.getItemId();
        StorageReference ref = storageRef.child(location);

        ImageServices.setImageWithGlide(this,ref,imgTheirItem);

        if(myItem.getTradeComplete() != null){
            btnProcessConfirm.setText("Waiting for response");
            btnProcessConfirm.setClickable(false);
        }

        btnProcessConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myItemRef.child("tradeComplete").setValue(true);
                if(theirItem.getTradeComplete() != null){
                    myItemRef.child("outMarket").removeValue();
                    myItemRef.child("tradeComplete").removeValue();
                    myItemRef.child("offers").removeValue();
                    myItemRef.child("matches").removeValue();
                    myItemRef.child("userId").setValue(otherUser.getUserId());
                    userRef.child("tradeItems").child(myItem.getItemId()).removeValue();
                    userRef.child("tradeItems").child(theirItem.getItemId()).setValue(true);
                    userRef.child("currentTradeItem").setValue(theirItem.getItemId());

                    theirItemRef.child("outMarket").removeValue();
                    theirItemRef.child("tradeComplete").removeValue();
                    theirItemRef.child("offers").removeValue();
                    theirItemRef.child("matches").removeValue();
                    theirItemRef.child("userId").setValue(fUser.getUid());
                    otherUserRef.child("tradeItems").child(theirItem.getItemId()).removeValue();
                    otherUserRef.child("tradeItems").child(myItem.getItemId()).setValue(true);
                    otherUserRef.child("currentTradeItem").setValue(myItem.getItemId());

                    onBackPressed();
                }else{
                    myItemRef.child("tradeComplete").setValue(true);
                    btnProcessConfirm.setText("Waiting for response");
                    btnProcessConfirm.setClickable(false);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent (ProcessActivity.this, TradeActivity.class);
        startActivity(intent);
    }
}
