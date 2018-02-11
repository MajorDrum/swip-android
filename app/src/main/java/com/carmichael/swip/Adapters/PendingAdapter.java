package com.carmichael.swip.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.carmichael.swip.Models.TradeItem;
import com.carmichael.swip.R;
import com.carmichael.swip.Services.ImageServices;
import com.carmichael.swip.Activities.ViewPendingActivity;
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

public class PendingAdapter extends RecyclerView.Adapter<PendingAdapter.ViewHolder>{

    private static final String TAG = "MatchAdapter";
    private TradeItem tradeItem;
    private ViewHolder viewHolder;
    private Context context;
    private TradeItem currentItem;
    private ArrayList<TradeItem> offerItems = new ArrayList<>();

    public PendingAdapter(TradeItem tradeItem, Context context) {
        this.tradeItem = tradeItem;
        this.context = context;
    }

    @Override
    public PendingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.record_offer, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mDatabase = database.getReference();
        String key = "hello";
        Log.d(TAG, "onBindViewHolder: key is: " + key);
        final DatabaseReference itemRef = mDatabase.child("TradeItems").child(key);

        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentItem = dataSnapshot.getValue(TradeItem.class);
                if(currentItem != null){
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        holder.offerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: position is: " + position);

                Intent intent = new Intent(context, ViewPendingActivity.class);
                intent.putExtra("TheirItemKey", offerItems.get(position).getItemId());
                intent.putExtra("MyItemKey", tradeItem.getItemId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    @Override
    public int getItemCount() {
        return 0;
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

