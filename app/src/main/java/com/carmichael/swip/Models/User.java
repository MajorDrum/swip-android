package com.carmichael.swip.Models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.carmichael.swip.Services.BitmapRetriever;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static android.content.ContentValues.TAG;

/**
 * Created by carte on 7/20/2017.
 */

public class User implements Parcelable {

    private String userId;
    private String firstName;
    private String lastName;
    private ArrayList<TradeItem> myItems;
    private int points;
    private String zipcode;
    private ArrayList<Offer> myOffers = new ArrayList<>();
    private String currentTradeItem;
    private String phone;
    private HashMap<String, Object> tradeItems;
    private FirebaseUser firebase;



    public User() {
        myItems = new ArrayList<>();
    }

    protected User(Parcel in) {
        myItems = in.createTypedArrayList(TradeItem.CREATOR);
        points = in.readInt();
        zipcode = in.readString();
        myOffers = in.createTypedArrayList(Offer.CREATOR);
        currentTradeItem = in.readString();
        phone = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(myItems);
        dest.writeInt(points);
        dest.writeString(zipcode);
        dest.writeTypedList(myOffers);
        dest.writeString(currentTradeItem);
        dest.writeString(phone);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public String toString() {
        return "User{" +
                ", myItems=" + myItems +
                ", points=" + points +
                ", zipcode='" + zipcode + '\'' +
                ", myOffers=" + myOffers +
                ", currentTradeItem='" + currentTradeItem + '\'' +
                ", phone='" + phone + '\'' +
                ", tradeItems=" + tradeItems +
                '}';
    }

    public FirebaseUser getFirebase() {
        return firebase;
    }

    public void setFirebase(FirebaseUser firebase) {
        this.firebase = firebase;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCurrentTradeItem() {
        return currentTradeItem;
    }

    public void setCurrentTradeItem(String currentTradeItem) {
        this.currentTradeItem = currentTradeItem;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public HashMap<String, Object> getTradeItems() {
        return tradeItems;
    }

    public void setTradeItems(HashMap<String, Object> tradeItems) {
        this.tradeItems = tradeItems;
    }

    public ArrayList<String> getItemKeyStrings(){
        Set<String> itemKeyStrings = tradeItems.keySet();
        ArrayList<String> keys = new ArrayList<>();

        for(String s : itemKeyStrings){
            keys.add(s);
        }
        return keys;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public ArrayList<TradeItem> getMyItems() {
        return myItems;
    }

    public void setMyItems(ArrayList<TradeItem> myItems) {
        this.myItems = myItems;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public ArrayList<Offer> getMyOffers() {
        return myOffers;
    }

    public void setMyOffers(ArrayList<Offer> myOffers) {
        this.myOffers = myOffers;
    }


}
