package com.carmichael.swip.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.carmichael.swip.Contracts.APIContract;
import com.carmichael.swip.Models.TradeItem;
import com.carmichael.swip.Models.User;
import com.carmichael.swip.ReviewOfferActivity;
import com.carmichael.swip.R;
import com.carmichael.swip.Services.FirebaseServices;
import com.carmichael.swip.Services.ImageServices;
import com.carmichael.swip.Services.WebServices;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by carte on 7/22/2017.
 */

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.ViewHolder>{

    private static final String TAG = "MatchAdapter";
    private TradeItem tradeItem;
    private Context context;
    private TradeItem currentItem;
    private ArrayList<TradeItem> matchItems = new ArrayList<>();
    private User user;

    public MatchAdapter(TradeItem tradeItem, Context context, User user) {
        this.tradeItem = tradeItem;
        this.context = context;
        this.user = user;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.record_offer, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        String key = tradeItem.getHashMapKeysAsStrings(tradeItem.getOffers()).get(position);
        RetrieveJsonTask retrieveJsonTask = new RetrieveJsonTask();
        try{
            String json = retrieveJsonTask.execute(APIContract.URL_DATABASE_TRADEITEMS+key,user.getToken()).get();
            currentItem = FirebaseServices.convertJsonToTradeItem(json,key);
        }catch(Exception e){
            Log.e(TAG, "onBindViewHolder: could not retrieve json", e);
        }

        holder.tvOfferItemName.setText(currentItem.getName());
        matchItems.add(currentItem);
        String location = "TradeItems/" + currentItem.getItemId();
        StorageReference ref = FirebaseServices.getStorageReference(location);
        ImageServices.setImageWithGlide(context,ref,holder.imgOfferItem);

        holder.offerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(context, ReviewOfferActivity.class);
            intent.putExtra("TheirItemKey", matchItems.get(position).getItemId());
            intent.putExtra("MyItemKey", tradeItem.getItemId());
            intent.putExtra("User",user);
            context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tradeItem.getHashMapKeysAsStrings(tradeItem.getOffers()).size();
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

class RetrieveJsonTask extends AsyncTask<String, String, String> {
    @Override
    protected String doInBackground(String... params) {
        String json = WebServices.getFirebaseJson(params[0],params[1]);
        return json;
    }
}

