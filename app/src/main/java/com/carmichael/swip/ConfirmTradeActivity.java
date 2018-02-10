package com.carmichael.swip;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.carmichael.swip.Models.TradeItem;
import com.carmichael.swip.Models.User;
import com.carmichael.swip.R;
import com.carmichael.swip.Services.ImageServices;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.content.ContentValues.TAG;

public class ConfirmTradeActivity extends Activity {

    private TextView tvTitle;
    private TextView tvItemName;
    private TextView tvItemDescription;
    private ImageView imgMyItem;
    private Button btnConfirmTrade;
    private DatabaseReference mDatabase;
    private FirebaseUser fUser;
    private User user;
    private TradeItem myItem;
    private TradeItem theirItem;
    private String theirItemKey;
    DatabaseReference myItemRef;
    DatabaseReference theirItemRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_trade);

        this.setFinishOnTouchOutside(true);

        theirItemKey = getIntent().getStringExtra("TheirItemKey");

        tvTitle = (TextView) findViewById(R.id.tvConfirmTitle);
        tvItemName = (TextView) findViewById(R.id.tvConfirmItemName);
        tvItemDescription = (TextView) findViewById(R.id.tvConfirmItemDescription);
        imgMyItem = (ImageView) findViewById(R.id.imgConfirmMyItem);
        btnConfirmTrade = (Button) findViewById(R.id.btnConfirmTrade);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference userRef = mDatabase.child("Users").child(fUser.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                myItemRef = mDatabase.child("TradeItems").child(user.getCurrentTradeItem());
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

    public void BeginActivity(){
        tvTitle.setText(Html.fromHtml("<b>"+theirItem.getName()+"</b>" + " for " + "<b>"+myItem.getName()+"</b>"));
        tvItemName.setText(theirItem.getName());
        tvItemDescription.setText(theirItem.getDescription());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String location = "TradeItems/" + theirItem.getItemId();
        StorageReference ref = storageRef.child(location);

        ImageServices.setImageWithGlide(this,ref,imgMyItem);

        btnConfirmTrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ConfirmTradeActivity.this);
                alertDialogBuilder
                        .setTitle("Confirm Trade")
                        .setMessage(Html.fromHtml("By confirming the trade, you will be given contact information for" +
                                " the owner of <b>" + theirItem.getItemId() + "</b>.  Your item will be removed from the " +
                                "market while you facilitate the trade.  You also forfeit all current offers.  Would you " +
                                "like to confirm this trade?"))
                        .setCancelable(true)
                        .setPositiveButton("YES",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                                myItemRef.child("outMarket").setValue(theirItem.getItemId());
                                theirItemRef.child("outMarket").setValue(myItem.getItemId());
                                Intent intent = new Intent(ConfirmTradeActivity.this, ProcessActivity.class);
                                intent.putExtra("User",user);
                                intent.putExtra("MyItemKey", myItem.getItemId());
                                intent.putExtra("TheirItemKey", theirItem.getItemId());
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
        });
    }
}
