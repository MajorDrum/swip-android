package com.carmichael.swip.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.carmichael.swip.Contracts.APIContract;
import com.carmichael.swip.Fragments.OffersFragment;
import com.carmichael.swip.Models.TradeItem;
import com.carmichael.swip.Models.User;
import com.carmichael.swip.R;
import com.carmichael.swip.Services.FirebaseServices;
import com.carmichael.swip.Services.ImageServices;
import com.carmichael.swip.Services.RetrieveJsonTask;
import com.carmichael.swip.Services.WebServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

public class OffersActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private static final String TAG = "OffersActivity";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout tabLayout;
    private int iCurrentTradeItem;
    private User user;
    private DatabaseReference myItemRef;
    private DatabaseReference theirItemRef;
    private TradeItem myItem;
    private TradeItem theirItem;
    private String myItemKey;
    private String theirItemKey;
    private TextView tvTitle;
    private TextView tvItemName;
    private TextView tvItemDescription;
    private ImageView imgTheirItem;
    private Button btnConfirmTrade;
    private Context context;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers_beta);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;
        mViewPager = (ViewPager) findViewById(R.id.container);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        user = getIntent().getParcelableExtra("User");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        for(int i = 0; i < user.getMyItems().size(); i++){
            tabLayout.addTab(tabLayout.newTab().setText(user.getMyItems().get(i).toString()));
        }
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(mViewPager);
        iCurrentTradeItem = getIntent().getIntExtra("iCurrentTradeItem", 0);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    Log.d(TAG, "getItem: first attempt at user: " + user.toString());
                    OffersFragment offersFragment = OffersFragment.newInstance(iCurrentTradeItem, user);
                    return offersFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "OFFERS";
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent (OffersActivity.this, TradeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }


    public void showDialog(String myKey, String theirKey){
        LayoutInflater factory = LayoutInflater.from(this);
        View view = factory.inflate(R.layout.activity_confirm_trade, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        tvTitle = (TextView) view.findViewById(R.id.tvConfirmTitle);
        tvItemName = (TextView) view.findViewById(R.id.tvConfirmItemName);
        tvItemDescription = (TextView) view.findViewById(R.id.tvConfirmItemDescription);
        imgTheirItem = (ImageView) view.findViewById(R.id.imgConfirmMyItem);
        btnConfirmTrade = (Button) view.findViewById(R.id.btnConfirmTrade);
        myItemKey = myKey;
        theirItemKey = theirKey;

        try{
            RetrieveJsonTask retrieveJsonTask = new RetrieveJsonTask();
            String url = APIContract.URL_DATABASE_TRADEITEMS+myItemKey;
            String myJson = retrieveJsonTask.execute(url,user.getToken()).get();
            myItem = FirebaseServices.convertJsonToTradeItem(myJson,myItemKey);
        }catch(Exception e){
            Log.e(TAG, "showDialog: unable to retrieve json", e);
        }

        try{
            RetrieveJsonTask retrieveJsonTask = new RetrieveJsonTask();
            String url = APIContract.URL_DATABASE_TRADEITEMS+theirItemKey;
            String myJson = retrieveJsonTask.execute(url,user.getToken()).get();
            theirItem = FirebaseServices.convertJsonToTradeItem(myJson,theirItemKey);
        }catch(Exception e){
            Log.e(TAG, "showDialog: unable to retrieve json", e);
        }

        tvTitle.setText(Html.fromHtml("<b>"+theirItem.getName()+"</b>" + " for " + "<b>"+myItem.getName()+"</b>"));
        tvItemName.setText(theirItem.getName());
        tvItemDescription.setText(theirItem.getDescription());

        String location = "TradeItems/" + theirItem.getItemId();
        StorageReference ref = FirebaseServices.getStorageReference(location);

        ImageServices.setImageWithGlide(context,ref,imgTheirItem);


        btnConfirmTrade.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               myItemRef = FirebaseDatabase.getInstance().getReference().child("TradeItems").child(myItemKey);
               theirItemRef = FirebaseDatabase.getInstance().getReference().child("TradeItems").child(theirItemKey);
               displayConfirmTradeDialog();
           }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void displayConfirmTradeDialog(){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setTitle("Confirm Trade")
                .setMessage(Html.fromHtml("By confirming the trade, you will be given contact information for" +
                        " the owner of <b>" + theirItem.getItemId() + "</b>.  Your item will be removed from the " +
                        "market while you facilitate the trade.  You also forfeit all current offers.  Would you " +
                        "like to confirm this trade?"))
                .setCancelable(true)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        myItemRef.child("outMarket").setValue(theirItem.getItemId());
                        theirItemRef.child("outMarket").setValue(myItem.getItemId());
                        Intent intent = new Intent(context, ProcessActivity.class);
                        intent.putExtra("User", user);
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
}
