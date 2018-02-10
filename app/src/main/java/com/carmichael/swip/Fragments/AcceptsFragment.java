package com.carmichael.swip.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.carmichael.swip.Adapters.AcceptAdapter;
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

public class AcceptsFragment extends Fragment {

    private static final String TAG = "AcceptsFragment";
    User user;
    AcceptAdapter acceptAdapter;
    ArrayList<TradeItem> tradeItems;
    RecyclerView rvOffers;
    TextView tvNoOfferMessage;
    Button btnReturnToTrade;
    TextView tvPoints;
    TextView tvYourOffers;
    boolean itemsReady = false;
    boolean userReady = false;
    FirebaseUser fUser;
    private int iCurrentTradeItem = 0;
    private View view;
    private ArrayList<TradeItem> myItems;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_accepts, container, false);
        return view;
    }


    public static AcceptsFragment newInstance(int exampleInt) {
        AcceptsFragment fragment = new AcceptsFragment();

        Bundle args = new Bundle();
        args.putInt("iCurrentTradeItem", exampleInt);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        iCurrentTradeItem = getArguments().getInt("iCurrentTradeItem");

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        tradeItems = new ArrayList<>();
        myItems = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        DatabaseReference itemRef = mDatabase.child("TradeItems");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        fUser = mAuth.getCurrentUser();

        final DatabaseReference userRef = mDatabase.child("Users").child(fUser.getUid());

        user = new User();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                userReady = true;
                if(userReady && itemsReady){
                    BeginActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        tradeItems = new ArrayList<>();

        itemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    TradeItem test = postSnapshot.getValue(TradeItem.class);
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
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void BeginActivity(){

        user.setMyItems(myItems);
        acceptAdapter = new AcceptAdapter(user.getMyItems().get(iCurrentTradeItem), getActivity());

        tvNoOfferMessage = (TextView) view.findViewById(R.id.tvAcceptNoOfferMessage);
        tvNoOfferMessage.setVisibility(View.GONE);

        btnReturnToTrade = (Button) view.findViewById(R.id.btnAcceptReturnToTrade);
        btnReturnToTrade.setVisibility(View.GONE);

        tvPoints = (TextView) view.findViewById(R.id.tvAcceptPoints);
        tvPoints.setText(Html.fromHtml("Your points: <b>" + user.getPoints() + "</b>"));

        tvYourOffers = (TextView) view.findViewById(R.id.tvAcceptYourOffers);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rvOffers = (RecyclerView) view.findViewById(R.id.rvAcceptOffers);
        rvOffers.setLayoutManager(linearLayoutManager);
        if(user.getMyItems().get(iCurrentTradeItem)
                .getHashMapKeysAsStrings(user.getMyItems()
                        .get(iCurrentTradeItem).getMatches()).size() > 0){
            rvOffers.setAdapter(acceptAdapter);
        }
    }
}
