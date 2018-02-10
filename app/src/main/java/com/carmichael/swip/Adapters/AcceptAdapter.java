package com.carmichael.swip.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.carmichael.swip.Models.TradeItem;
import com.carmichael.swip.Models.User;
import com.carmichael.swip.ProcessActivity;
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

import java.util.ArrayList;

/**
 * Created by carte on 7/22/2017.
 */

public class AcceptAdapter extends RecyclerView.Adapter<MatchAdapter.ViewHolder>{

    private static final String TAG = "AcceptAdapter";
    private TradeItem tradeItem;
    private ViewHolder viewHolder;
    private TradeItem currentItem;
    private ArrayList<TradeItem> offerItems = new ArrayList<>();
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

    public AcceptAdapter(TradeItem tradeItem, Context context) {
        this.tradeItem = tradeItem;
        this.context = context;
    }

    @Override
    public MatchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.record_offer, parent, false);
        MatchAdapter.ViewHolder vh = new MatchAdapter.ViewHolder(v);
        return vh;
    }



    @Override
    public void onBindViewHolder(final MatchAdapter.ViewHolder holder, final int position) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        final String key = tradeItem.getHashMapKeysAsStrings(tradeItem.getMatches()).get(position);
        final DatabaseReference itemRef = mDatabase.child("TradeItems").child(key);

        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentItem = dataSnapshot.getValue(TradeItem.class);
                currentItem.setItemId(dataSnapshot.getKey());
                holder.tvOfferItemName.setText(currentItem.getName());

                offerItems.add(currentItem);
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                String location = "TradeItems/" + currentItem.getItemId();
                StorageReference ref = storageRef.child(location);
                Log.d(TAG, "onClick: item id is: " + currentItem.getItemId());

                ImageServices.setImageWithGlide(context,ref,holder.imgOfferItem);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        holder.offerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(key);
            }
        });


    }

    public void showDialog(String key){
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.activity_confirm_trade, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        tvTitle = (TextView) view.findViewById(R.id.tvConfirmTitle);
        tvItemName = (TextView) view.findViewById(R.id.tvConfirmItemName);
        tvItemDescription = (TextView) view.findViewById(R.id.tvConfirmItemDescription);
        imgTheirItem = (ImageView) view.findViewById(R.id.imgConfirmMyItem);
        btnConfirmTrade = (Button) view.findViewById(R.id.btnConfirmTrade);


        myItemKey = tradeItem.getItemId();
        theirItemKey = key;



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
                                                        " the owner of <b>" + theirItem.getName() + "</b>.  Your item will be removed from the " +
                                                        "market while you facilitate the trade.  You also forfeit all current offers.  Would you " +
                                                        "like to confirm this trade?"))
                                                .setCancelable(true)
                                                .setPositiveButton("YES",new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,int id) {
                                                        theirItemRef.child("matches").setValue(myItemKey);
                                                        myItemRef.child("outMarket").setValue(theirItem.getItemId());
                                                        theirItemRef.child("outMarket").setValue(myItem.getItemId());
                                                        Intent intent = new Intent(context, ProcessActivity.class);
                                                        intent.putExtra("MyItemKey", myItem.getItemId());
                                                        intent.putExtra("TheirItemKey", theirItem.getItemId());
                                                        context.startActivity(intent);
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


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    @Override
    public int getItemCount() {
        return tradeItem.getHashMapKeysAsStrings(tradeItem.getMatches()).size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tvOfferItemName;
        public ImageView imgOfferItem;
        public LinearLayout offerLayout;


        public ViewHolder(View offerView) {
            super(offerView);
            tvOfferItemName = (TextView) offerView.findViewById(R.id.tvOfferItemName);
            imgOfferItem = (ImageView) offerView.findViewById(R.id.imgOfferItem);
            offerLayout = (LinearLayout) offerView.findViewById(R.id.offer_layout);
        }

    }
}

