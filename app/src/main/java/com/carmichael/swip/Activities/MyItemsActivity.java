package com.carmichael.swip.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;

import com.carmichael.swip.Adapters.MyItemsAdapter;
import com.carmichael.swip.Models.User;
import com.carmichael.swip.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyItemsActivity extends AppCompatActivity {

    private static final String TAG = "MyItemsActivity";
    private User user;
    private RecyclerView rvMyItems;
    private MyItemsAdapter myItemsAdapter;
    private String recentItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_items);

        user = getIntent().getParcelableExtra("User");
        user.initFirebase();
        rvMyItems = (RecyclerView) findViewById(R.id.rvMyItems);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        myItemsAdapter = new MyItemsAdapter(user.getMyItems(), this, user);
        rvMyItems.setLayoutManager(linearLayoutManager);
        rvMyItems.setAdapter(myItemsAdapter);
        recentItemId = getIntent().getStringExtra("RecentItemId");
    }

    public void switchItem(final int position){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MyItemsActivity.this);
        alertDialogBuilder.setTitle("Switch Items");
        alertDialogBuilder
                .setMessage(Html.fromHtml("Would you like to select <b>" + user.getMyItems().get(position).getName() +
                        "</b> as your current item?"))
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference currentItemRef =
                                mDatabase.child("Users").child(user.getFirebase().getUid()).child("currentTradeItem");
                        currentItemRef.setValue(user.getMyItems().get(position).getItemId());
                        Intent intent = new Intent(MyItemsActivity.this, TradeActivity.class);
                        intent.putExtra("RecentItemId", recentItemId);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MyItemsActivity.this, TradeActivity.class);
        intent.putExtra("RecentItemId", recentItemId);
        startActivity(intent);
    }
}
