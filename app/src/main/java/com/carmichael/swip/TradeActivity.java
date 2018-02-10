package com.carmichael.swip;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterViewFlipper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.carmichael.swip.Adapters.TradeItemAdapter;
import com.carmichael.swip.Contracts.APIContract;
import com.carmichael.swip.Models.TradeItem;
import com.carmichael.swip.Models.User;
import com.carmichael.swip.Services.ImageServices;
import com.carmichael.swip.Services.WebServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class TradeActivity extends AppCompatActivity {

    private static final String TAG = "TradeActivity";
    public AdapterViewFlipper flipperTradeItems;
    private ArrayList<TradeItem> tradeItems;
    private TextView tvTradeItem;
    private TextView tvMyItem;
    private ImageView imgMyItem;
    private TextView tvOfferSent;
    private User user;
    private Float[] rotationArray = {(float)38, (float)-38, (float)45, (float)-45,(float) 18,
            (float)-18,(float) 27,(float) -27};
    private GestureDetector mGestureDetector;
    private TradeItemAdapter tradeItemAdapter;
    private Context context = this;
    private Button btnAccept;
    private Button btnDeny;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private int iCurrentTradeItem = 0;
    private ArrayList<TradeItem> myItems;
    private DatabaseReference userRef;
    private ConstraintLayout constraintLoading;
    private ProgressBar spinnerLoading;
    private String token;

    String ENDPOINT = "https://api.myjson.com/bins/147n0l";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        initActivity();

        // Retrieve all data and begin activity after token is retrieved
        user.getFirebase().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                token = task.getResult().getToken();
                try{
                    Gson gson = new Gson();

                    // Get user
                    String url = String.format(APIContract.URL_DATABASE_BEG+"/%s/%s",
                                                    "Users",
                                                    user.getFirebase().getUid());
                    String json = new RetrieveJsonTask().execute(url,token).get();
                    user = gson.fromJson(json,User.class);
                    user.setFirebase(mAuth.getCurrentUser());

                    // Get ALL TradeItems
                    url = String.format(APIContract.URL_DATABASE_BEG+"/%s",
                                                "TradeItems");
                    json = new RetrieveJsonTask().execute(url,token).get();

                    // Firebase returns a JSON object instead of array, so we have to manually
                    // convert it
                    JSONObject jsonObject = new JSONObject(json);
                    Iterator x = jsonObject.keys();
                    JSONArray jsonArray = new JSONArray();

                    while (x.hasNext()){
                        String key = (String) x.next();
                        JSONObject toPut = new JSONObject(jsonObject.get(key).toString());
                        toPut.put("itemId",key);
                        jsonArray.put(toPut);
                    }
                    tradeItems = gson.fromJson(jsonArray.toString(), new TypeToken<List<TradeItem>>(){}.getType());

                    // Load items into user
                    for(TradeItem tradeItem : tradeItems){
                        if (tradeItem.getUserId().equals(user.getFirebase().getUid())){
                            myItems.add(tradeItem);
                        }
                    }
                    beginTrading();
                }catch(Exception e){
                    Log.e(TAG, "onComplete: TestActivity could not retrieve JSON",e);
                }
            }
        });
    }

    private class RetrieveJsonTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String json = WebServices.getFirebaseJson(params[0],params[1]);
            return json;
        }
    }

    public void beginTrading(){
        Log.d(TAG, "beginTrading: starts");

        initTradingView();

        // Set currentTradeItem to first item if there is none set
        if(user.getCurrentTradeItem() == null && user.getMyItems().size() > 0){
            user.setCurrentTradeItem(user.getMyItems().get(0).getItemId());
            userRef.child("currentTradeItem").setValue(user.getMyItems().get(0).getItemId());
        }

        // Figure out where currentTradeItem is in the array for easy retrieval
        for(int i = 0; i < user.getMyItems().size(); i++){
            if(user.getMyItems().get(i).getItemId().equals(user.getCurrentTradeItem())){
                iCurrentTradeItem = i;
            }
        }

        if(user.getMyItems().size() > 0){
            initHasItemsView();
        }else{
            initNoItemsView();
        }

        // Randomize items (this will change later)
        Collections.shuffle(tradeItems);

        putRecentItemAtFront();

        tradeItemAdapter = new TradeItemAdapter(TradeActivity.this, tradeItems, tvTradeItem, flipperTradeItems);
        flipperTradeItems.setAdapter(tradeItemAdapter);
        imgMyItem.setOnClickListener(manageItemsListener);
    }

    View.OnClickListener manageItemsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent manageIntent = new Intent(TradeActivity.this, MyItemsActivity.class);
            manageIntent.putExtra("User", user);
            manageIntent.putExtra("RecentItemId", tradeItems.get(flipperTradeItems.getDisplayedChild()).getItemId());
            startActivity(manageIntent);
        }
    };

    public void initActivity(){
        constraintLoading = (ConstraintLayout) findViewById(R.id.constraint_loading);
        spinnerLoading = (ProgressBar) findViewById(R.id.spinner_loading);
        flipperTradeItems = (AdapterViewFlipper) findViewById(R.id.flipperTradeItems);
        btnAccept = (Button) findViewById(R.id.btnAccept);
        btnDeny = (Button) findViewById(R.id.btnDeny);
        tvTradeItem = (TextView) findViewById(R.id.tvTradeItem);
        tvMyItem = (TextView) findViewById(R.id.tvMyItem);
        imgMyItem = (ImageView) findViewById(R.id.imgMyItem);
        tvOfferSent = (TextView) findViewById(R.id.tvOfferSent);
        flipperTradeItems.setVisibility(View.GONE);
        btnAccept.setVisibility(View.GONE);
        btnDeny.setVisibility(View.GONE);
        tvTradeItem.setVisibility(View.GONE);
        tvMyItem.setVisibility(View.GONE);
        imgMyItem.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();
        user = new User();
        user.setFirebase(mAuth.getCurrentUser());
        tvOfferSent.setVisibility(View.GONE);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userRef = mDatabase.child("Users").child(user.getFirebase().getUid());
        tradeItems = new ArrayList<>();
        myItems = new ArrayList<>();
    }

    public void putRecentItemAtFront(){
        String recentItemId = getIntent().getStringExtra("RecentItemId");
        if(recentItemId != null){
            TradeItem mostRecentItem = null;
            for(int i = 0; i < tradeItems.size(); i++){
                if(tradeItems.get(i).getItemId().equals(recentItemId)){
                    mostRecentItem = tradeItems.get(i);
                }
            }
            tradeItems.remove(mostRecentItem);
            tradeItems.add(mostRecentItem);

            // Reverse collection so it appears at the front
            Collections.reverse(tradeItems);
        }
    }

    public void initTradingView(){
        spinnerLoading.setVisibility(View.GONE);
        constraintLoading.setVisibility(View.GONE);
        flipperTradeItems.setVisibility(View.VISIBLE);
        btnAccept.setVisibility(View.VISIBLE);
        btnDeny.setVisibility(View.VISIBLE);
        tvTradeItem.setVisibility(View.VISIBLE);
        tvMyItem.setVisibility(View.VISIBLE);
        imgMyItem.setVisibility(View.VISIBLE);
        user.setMyItems(myItems);
    }

    public void initNoItemsView(){
        tvMyItem.setText("Upload an item to begin trading!");
        btnAccept.setText("Upload an Item");
        btnDeny.setText("Upload an Item");
        flipperTradeItems.setVisibility(View.INVISIBLE);
        tvOfferSent.setText("Enter an item to view possible trades!");
        tvTradeItem.setVisibility(View.INVISIBLE);
        tvOfferSent.setVisibility(View.VISIBLE);
        tvOfferSent.setBackgroundColor(getResources().getColor(R.color.white));
        tvOfferSent.setTextColor(getResources().getColor(R.color.black));
        tvOfferSent.setRotation(0);
        tvOfferSent.setTextSize(28);
        ViewGroup.LayoutParams params = tvOfferSent.getLayoutParams();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        tvOfferSent.setLayoutParams(params);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent uploadIntent = new Intent(TradeActivity.this, UploadActivity.class);
                startActivity(uploadIntent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            }
        });

        btnDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent uploadIntent = new Intent(TradeActivity.this, UploadActivity.class);
                startActivity(uploadIntent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            }
        });
    }

    public void initHasItemsView(){
        tvMyItem.setText(user.getMyItems().get(iCurrentTradeItem).getName());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference itemRef = storage.getReference();
        String itemLocation = "TradeItems/" + user.getMyItems().get(iCurrentTradeItem).getItemId();
        StorageReference ref = itemRef.child(itemLocation);

        ImageServices.setImageCircularWithGlide(imgMyItem,ref,this);
        flipperTradeItems.setInAnimation(TradeActivity.this, R.animator.slide_in_down);
        flipperTradeItems.setOutAnimation(TradeActivity.this, R.animator.slide_out_right);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accept();
            }
        });
        btnDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deny();
            }
        });
        CustomGestureDetector customGestureDetector = new CustomGestureDetector();
        mGestureDetector = new GestureDetector(this, customGestureDetector);
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.constraint_flipper);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mGestureDetector.onTouchEvent(event)) {
                    return false;
                }
                return true;
            }
        });
    }


    public void accept(){
        flipperTradeItems.setOutAnimation(TradeActivity.this, R.animator.slide_out_left);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mDatabase = database.getReference();
        TradeItem currentItem = tradeItems.get(flipperTradeItems.getDisplayedChild());
        String currentItemId = currentItem.getItemId();
        mDatabase.child("TradeItems").child(currentItemId).child("offers").child(user.getCurrentTradeItem()).setValue("true");

        if(user.getMyItems().get(iCurrentTradeItem)
                .getHashMapKeysAsStrings(user.getMyItems()
                        .get(iCurrentTradeItem).getOffers()).contains(currentItemId)){
            mDatabase.child("TradeItems").child(user.getCurrentTradeItem())
                    .child("matches").child(currentItemId).setValue("true");
            mDatabase.child("TradeItems").child(currentItemId)
                    .child("matches").child(user.getCurrentTradeItem()).setValue("true");
        }

        displayOfferSent();
    }

    public void displayOfferSent(){
        Random rand = new Random();
        tvOfferSent.setRotation(rotationArray[rand.nextInt(8)]);
        tvOfferSent.setVisibility(View.VISIBLE);
        Animation fadeout = new AlphaAnimation(1.f, 0.f);
        fadeout.setDuration(800);
        tvOfferSent.startAnimation(fadeout);
        tvOfferSent.postDelayed(new Runnable() {
            @Override
            public void run() {
                flipperTradeItems.showNext();
                tvOfferSent.setVisibility(View.INVISIBLE);
            }
        }, 400);
        int points = user.getPoints() + 1;
        user.setPoints(points);
    }

    public void deny(){
        flipperTradeItems.setOutAnimation(TradeActivity.this, R.animator.slide_out_right);
        int points = user.getPoints() + 1;
        user.setPoints(points);
        flipperTradeItems.showNext();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.offer_menu:
                if(user.getMyItems().get(iCurrentTradeItem).getOutMarket() == null){
                    Intent intent = new Intent(TradeActivity.this, OffersActivity.class);
                    intent.putExtra("iCurrentTradeItem", iCurrentTradeItem);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(TradeActivity.this, ProcessActivity.class);
                    intent.putExtra("MyItemKey", user.getMyItems().get(iCurrentTradeItem).getItemId());
                    intent.putExtra("TheirItemKey", user.getMyItems().get(iCurrentTradeItem).getOutMarket());
                    startActivity(intent);
                }
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                return true;
            case R.id.cash_out:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TradeActivity.this);
                alertDialogBuilder.setTitle("Cash Out");
                alertDialogBuilder
                        .setMessage(Html.fromHtml("Would you like to stop trying to trade your <b>" + user.getMyItems().get(iCurrentTradeItem).getName() +
                                "</b> and receive it instead?"))
                        .setCancelable(false)
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.dismiss();
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TradeActivity.this);
                                alertDialogBuilder
                                        .setMessage("Arrangements will be made for you to receive your item shortly!  " +
                                                "Thanks for playing!  Please take our survey about your experience!")
                                        .setCancelable(false)
                                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,int id) {
                                                String url = "https://docs.google.com/forms/d/e/1FAIpQLSezchTj7OzL9dpyArra47dVezXIJ4gN64mpaBKcLwgLk-_87A/viewform";
                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                                startActivity(browserIntent);
                                                finish();
                                                System.exit(0);
                                            }
                                        });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
            case R.id.upload_item:
                Intent uploadIntent = new Intent(TradeActivity.this, UploadActivity.class);
                startActivity(uploadIntent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                return true;
            case R.id.manage_items:
                Intent manageIntent = new Intent(TradeActivity.this, MyItemsActivity.class);
                manageIntent.putExtra("User", user);
                startActivity(manageIntent);
                return true;
            case R.id.menu_test:
                Intent testIntent = new Intent(TradeActivity.this, TestActivity.class);
                startActivity(testIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            tradeItemAdapter.showItemPictures();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Swipe left (next)
            if (e1.getX() > e2.getX()) {
                deny();
            }

            // Swipe right (previous)
            if (e1.getX() < e2.getX()) {
                accept();
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

}
