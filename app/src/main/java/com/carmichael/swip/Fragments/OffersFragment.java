package com.carmichael.swip.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.carmichael.swip.Adapters.MatchAdapter;
import com.carmichael.swip.Contracts.APIContract;
import com.carmichael.swip.Models.TradeItem;
import com.carmichael.swip.Models.User;
import com.carmichael.swip.R;
import com.carmichael.swip.Services.FirebaseServices;
import com.carmichael.swip.Services.RetrieveJsonTask;
import com.carmichael.swip.Services.WebServices;

import java.util.ArrayList;

/**
 * Created by carte on 9/5/2017.
 */

public class OffersFragment extends Fragment {

    private static final String TAG = "OffersFragment";
    private User user;
    private MatchAdapter matchAdapter;
    private ArrayList<TradeItem> tradeItems;
    private RecyclerView rvOffers;
    private TextView tvNoOfferMessage;
    private Button btnReturnToTrade;
    private TextView tvPoints;
    private TextView tvYourOffers;
    private int iItem = 0;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_offers, container, false);
        return view;
    }

    public static OffersFragment newInstance(int exampleInt, User user) {
        OffersFragment fragment = new OffersFragment();
        Bundle args = new Bundle();
        args.putInt("iItem", exampleInt);
        args.putParcelable("User",user);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        iItem = getArguments().getInt("iItem");
        user = getArguments().getParcelable("User");

        // Get ALL TradeItems and add them to user's items if match
        try{
            String json = new RetrieveJsonTask().execute(APIContract.URL_DATABASE_TRADEITEMS,user.getToken()).get();
            tradeItems = FirebaseServices.convertTradeItemJsonToArray(json);
            ArrayList<TradeItem> myItems = new ArrayList<>();
            for(TradeItem tradeItem : tradeItems){
                if(tradeItem.getUserId().equals(user.getUserId())){
                    myItems.add(tradeItem);
                }
            }
            user.setMyItems(myItems);
        }catch(Exception e){
            Log.e(TAG, "onActivityCreated: could not return all tradeItems", e);
        }

        beginActivity();
    }

    public void beginActivity(){
        matchAdapter = new MatchAdapter(user.getMyItems().get(iItem), getActivity(), user);
        tvNoOfferMessage = (TextView) view.findViewById(R.id.tvNoOfferMessage);
        tvNoOfferMessage.setVisibility(View.GONE);
        btnReturnToTrade = (Button) view.findViewById(R.id.btnReturnToTrade);
        btnReturnToTrade.setVisibility(View.GONE);
        tvPoints = (TextView) view.findViewById(R.id.tvPoints);
        tvPoints.setText(Html.fromHtml("Your points: <b>" + user.getPoints() + "</b>"));
        tvYourOffers = (TextView) view.findViewById(R.id.tvYourOffers);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvOffers = (RecyclerView) view.findViewById(R.id.rvOffers);
        rvOffers.setLayoutManager(linearLayoutManager);
        if(user.getMyItems().get(iItem)
                .getHashMapKeysAsStrings(user.getMyItems()
                        .get(iItem).getOffers()).size() > 0){
            Log.d(TAG, "beginActivity: item greater than zero");
            rvOffers.setAdapter(matchAdapter);
        }
        tvYourOffers = (TextView) view.findViewById(R.id.tvYourOffers);
        tvYourOffers.setText("Trade your "+
                user.getMyItems().get(iItem).getName()+" for:");

    }
}
