package com.carmichael.swip.Fragments;

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
import com.carmichael.swip.Models.TradeItem;
import com.carmichael.swip.Models.User;
import com.carmichael.swip.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by carte on 9/5/2017.
 */

public class OffersFragment extends Fragment {

    private static final String TAG = "OffersFragment";
    User user;
    MatchAdapter matchAdapter;
    ArrayList<TradeItem> tradeItems;
    RecyclerView rvOffers;
    TextView tvNoOfferMessage;
    Button btnReturnToTrade;
    TextView tvPoints;
    TextView tvYourOffers;
    boolean itemsReady = false;
    boolean userReady = false;
    FirebaseUser fUser;
    private int iItem = 0;
    private View view;
    private ArrayList<TradeItem> myItems;

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

        Log.d(TAG, "onActivityCreated: second attempt at user: " + user.toString());

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();


        tradeItems = new ArrayList<>();


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        DatabaseReference itemRef = mDatabase.child("TradeItems");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        fUser = mAuth.getCurrentUser();

        final DatabaseReference userRef = mDatabase.child("Users").child(fUser.getUid());




        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                userReady = true;
                user = getArguments().getParcelable("User");
                if(userReady && itemsReady){
                    BeginActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        tradeItems = new ArrayList<>();
        myItems = new ArrayList<>();

        itemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    TradeItem test = postSnapshot.getValue(TradeItem.class);
                    Log.d(TAG, "onDataChange: offers items are: " + test.toString());
                    test.setItemId(postSnapshot.getKey());
                    if(test.getUserId().equals(fUser.getUid())){
                        myItems.add(test);
                    }else{
                        tradeItems.add(test);
                    }
                }
                itemsReady = true;
                if(userReady && itemsReady){
                    BeginActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void BeginActivity(){

        Log.d(TAG, "onActivityCreated: bundle user is: " + user.toString());
        user.setMyItems(myItems);

        Log.d(TAG, "BeginActivity: my item is: " + user.getMyItems().get(iItem).toString());

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

        rvOffers = (RecyclerView) view.findViewById(R.id.rvOffers);
        rvOffers.setLayoutManager(linearLayoutManager);
        if(user.getMyItems().get(iItem)
                .getHashMapKeysAsStrings(user.getMyItems()
                        .get(iItem).getOffers()).size() > 0){
            rvOffers.setAdapter(matchAdapter);
        }

        tvYourOffers = (TextView) view.findViewById(R.id.tvYourOffers);
        tvYourOffers.setText("Trade your "+
                user.getMyItems().get(iItem).getName()+" for:");



//        if(user.getMyOffers().size() == 0){
//            tvYourOffers.setVisibility(View.GONE);
//            rvPending.setVisibility(View.GONE);
//            tvNoOfferMessage.setVisibility(View.VISIBLE);
//            btnReturnToTrade.setVisibility(View.VISIBLE);
//            tvNoOfferMessage.setText(Html.fromHtml("You have no offers for your current " +
//                    "item: <b>" + user.getMyItems().get(0).getName() + "</b>.")); // Temporary
//        }
    }
}
