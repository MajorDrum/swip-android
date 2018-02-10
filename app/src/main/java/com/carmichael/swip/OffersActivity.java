package com.carmichael.swip;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.carmichael.swip.Fragments.AcceptsFragment;
import com.carmichael.swip.Fragments.OffersFragment;
import com.carmichael.swip.Fragments.PendingFragment;
import com.carmichael.swip.Models.TradeItem;
import com.carmichael.swip.Models.User;
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
    private FirebaseUser fUser;
    private User user;
    DatabaseReference mDatabase;
    DatabaseReference myItemRef;
    DatabaseReference theirItemRef;
    TradeItem myItem;
    TradeItem theirItem;
    String myItemKey;
    String theirItemKey;
    private TextView tvTitle;
    private TextView tvItemName;
    private TextView tvItemDescription;
    private ImageView imgTheirItem;
    private Button btnConfirmTrade;
    private Context context;
    private DatabaseReference userRef;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers_beta);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.


        context = this;

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);


        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                Log.d(TAG, "onDataChange: user added");

                mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

                mViewPager.setAdapter(mSectionsPagerAdapter);

                for(int i = 0; i < user.getMyItems().size(); i++){
                    tabLayout.addTab(tabLayout.newTab().setText(user.getMyItems().get(i).toString()));
                }
                tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

                tabLayout.setupWithViewPager(mViewPager);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });


        iCurrentTradeItem = getIntent().getIntExtra("iCurrentTradeItem", 0);


    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    OffersFragment offersFragment = OffersFragment.newInstance(iCurrentTradeItem);
                    return offersFragment;
//                case 1:
//                    PendingFragment pendingFragment = PendingFragment.newInstance(iCurrentTradeItem);
//                    return  pendingFragment;
//                case 2:
//                    AcceptsFragment acceptsFragment = AcceptsFragment.newInstance(iCurrentTradeItem);
//                    return acceptsFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
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
                                tvTitle.setText(Html.fromHtml("<b>"+theirItem.getName()+"</b>" + " for " + "<b>"+myItem.getName()+"</b>"));
                                tvItemName.setText(theirItem.getName());
                                tvItemDescription.setText(theirItem.getDescription());

                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference storageRef = storage.getReference();
                                String location = "TradeItems/" + theirItem.getItemId();
                                StorageReference ref = storageRef.child(location);

                                ImageServices.setImageWithGlide(context,ref,imgTheirItem);


                                btnConfirmTrade.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
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
                                                        Intent intent = new Intent(context, ProcessActivity.class);
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

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
