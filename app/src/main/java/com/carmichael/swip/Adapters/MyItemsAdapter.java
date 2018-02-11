package com.carmichael.swip.Adapters;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;


import com.carmichael.swip.Models.TradeItem;
import com.carmichael.swip.Models.User;
import com.carmichael.swip.Activities.MyItemsActivity;
import com.carmichael.swip.R;
import com.carmichael.swip.Services.ImageServices;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by carte on 7/22/2017.
 */

public class MyItemsAdapter extends RecyclerView.Adapter<MyItemsAdapter.ViewHolder>{

    private static final String TAG = "MatchAdapter";
    private ArrayList<TradeItem> tradeItemsList;
    private MyItemsActivity context;
    private User user;


    public MyItemsAdapter(ArrayList<TradeItem> tradeItemsList, MyItemsActivity context, User user) {
        this.tradeItemsList = tradeItemsList;
        this.context = context;
        this.user = user;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.record_my_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final TradeItem currentItem = tradeItemsList.get(position);

        holder.tvMyItemTitle.setText(currentItem.getName());
        Log.d(TAG, "onBindViewHolder: user current trade item: " + user.getCurrentTradeItem());
        Log.d(TAG, "onBindViewHolder: trade item: " + currentItem.getItemId());

        if(currentItem.getItemId().equals(user.getCurrentTradeItem())){
            holder.rdoMyItemSelection.setChecked(true);
        }

        try{
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            String location = "TradeItems/" + currentItem.getItemId();
            StorageReference ref = storageRef.child(location);
            ImageServices.setImageWithGlide(context,ref,holder.imgMyItem);
        }catch (Exception e){
            Log.e(TAG, "getView: error is: " + e.getStackTrace());
        }

        holder.myItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.switchItem(position);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    @Override
    public int getItemCount() {
        return tradeItemsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tvMyItemTitle;
        public ImageView imgMyItem;
        public RadioButton rdoMyItemSelection;
        public ConstraintLayout myItemLayout;


        public ViewHolder(View myItemView) {
            super(myItemView);
            tvMyItemTitle = (TextView) myItemView.findViewById(R.id.tvMyItemTitle);
            imgMyItem = (ImageView) myItemView.findViewById(R.id.imgMyItem);
            rdoMyItemSelection = (RadioButton) myItemView.findViewById(R.id.rdoMyItemSelection);
            myItemLayout = (ConstraintLayout) myItemView.findViewById(R.id.myItemLayout);
        }

    }
}

