package com.carmichael.swip.Services;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

/**
 * Created by carte on 2/9/2018.
 */

public class ImageServices {
    public static void setImageCircularWithGlide(final ImageView imageView, StorageReference reference, final Context context){
        Glide.with(context).using(new FirebaseImageLoader()).load(reference)
                .asBitmap().centerCrop().into(new BitmapImageViewTarget(imageView) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                imageView.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    public static void setImageWithGlide(Context context, StorageReference reference, ImageView imageView){
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(reference)
                .into(imageView);
    }
}
