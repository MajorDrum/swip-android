package com.carmichael.swip.Adapters;

import android.support.v7.app.AlertDialog;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.carmichael.swip.Models.TradeItem;
import com.carmichael.swip.Services.ImageServices;
import com.carmichael.swip.Activities.TradeActivity;
import com.carmichael.swip.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


/**
 * Adapter for viewflipper
 */

public class TradeItemAdapter extends BaseAdapter {

    private static final String TAG = "TradeItemAdapter";
    ArrayList<TradeItem> tradeItems;
    TradeActivity context;
    TextView tvItemName;
    ViewFlipper flipperMultiple;
    View multipleItemView;
    ImageView image;
    ImageView image1;
    ImageView image2;
    ImageView image3;
    AdapterViewFlipper flipperTradeItems;
    View descriptionView;
    TextView tvMultipleItemTitle;
    TextView tvMultipleItemDescription;


    public TradeItemAdapter(TradeActivity context, ArrayList<TradeItem> tradeItems, TextView tvItemName, AdapterViewFlipper flipperTradeItems) {
        this.tradeItems = tradeItems;
        this.context = context;
        this.tvItemName = tvItemName;
        multipleItemView = context.getLayoutInflater().inflate(R.layout.tradeitem_multiple_view, null);
        image = new ImageView(context);
        image1 = new ImageView(context);
        image1.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image2 = new ImageView(context);
        image3 = new ImageView(context);
        this.flipperTradeItems = flipperTradeItems;
        flipperMultiple = (ViewFlipper) multipleItemView.findViewById(R.id.flipperMultiple);
        descriptionView = context.getLayoutInflater().inflate(R.layout.description_view, null);
        tvMultipleItemTitle = (TextView) descriptionView.findViewById(R.id.tvMultipleItemTitle);
        tvMultipleItemDescription = (TextView) descriptionView.findViewById(R.id.tvMultipleItemDescription);

        CustomGestureDetector customGestureDetector = new CustomGestureDetector();
        final GestureDetector mGestureDetector = new GestureDetector(context, customGestureDetector);

        flipperMultiple.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mGestureDetector.onTouchEvent(event)) {
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public int getCount() {
        return tradeItems.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TradeItem currentTradeItem = tradeItems.get(position);
        final ImageView image = new ImageView(context);


//        ImageView image2 = new ImageView(context);
//        ImageView image3 = new ImageView(context);

//        image.setAdjustViewBounds(true);
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        image.setLayoutParams(params);


//        Drawable background = context.getResources().getDrawable(R.drawable.view_border);
//        image.setBackground(background);


        tvMultipleItemTitle.setText(currentTradeItem.getName());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String location = "TradeItems/" + currentTradeItem.getItemId();
        StorageReference ref = storageRef.child(location);

        ImageServices.setImageWithGlide(context,ref,image);


//        flipperMultiple.removeAllViews();
//        flipperMultiple.addView(image1);
//        flipperMultiple.addView(descriptionView);

        tvItemName.setText(currentTradeItem.getName());
        if (currentTradeItem.getDescription() == "") {
            tvMultipleItemDescription.setText("The owner of this item has not provided a description.");
        } else {
            tvMultipleItemDescription.setText(currentTradeItem.getDescription());
        }

        return image;
    }

    public void showItemPictures() {
//        if(multipleItemView.getParent() != null){
//            ((ViewGroup)multipleItemView.getParent()).removeAllViews();
//        }
        if (descriptionView.getParent() != null) {
            ((ViewGroup) descriptionView.getParent()).removeAllViews();
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(descriptionView);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();

        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            // Swipe left (next)
            if (e1.getX() > e2.getX()) {
                flipperMultiple.setInAnimation(context, R.anim.slide_in_right_quick);
                flipperMultiple.setOutAnimation(context, R.anim.slide_out_right_quick);
                flipperMultiple.showNext();
            }

            // Swipe right (previous)
            if (e1.getX() < e2.getX()) {
                flipperMultiple.setInAnimation(context, R.anim.slide_in_left_quick);
                flipperMultiple.setOutAnimation(context, R.anim.slide_out_left_quick);
                flipperMultiple.showPrevious();
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return tradeItems.get(position);
    }
}




