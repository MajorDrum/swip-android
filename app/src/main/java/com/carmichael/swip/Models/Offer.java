package com.carmichael.swip.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.carmichael.swip.Models.TradeItem;

/**
 * Created by carte on 7/20/2017.
 */

public class Offer implements Parcelable {
    private TradeItem myItem;
    private TradeItem theirItem;
    private int arrayPosition; // Demo purposes only

    public Offer(){}

    public Offer(TradeItem myItem, TradeItem theirItem) {
        this.myItem = myItem;
        this.theirItem = theirItem;
    }

    public Offer(TradeItem myItem, TradeItem theirItem, int arrayPosition) {
        this.myItem = myItem;
        this.theirItem = theirItem;
        this.arrayPosition = arrayPosition;
    }

    public int getArrayPosition() {
        return arrayPosition;
    }

    public void setArrayPosition(int arrayPosition) {
        this.arrayPosition = arrayPosition;
    }

    public TradeItem getMyItem() {
        return myItem;
    }

    public void setMyItem(TradeItem myItem) {
        this.myItem = myItem;
    }

    public TradeItem getTheirItem() {
        return theirItem;
    }

    public void setTheirItem(TradeItem theirItem) {
        this.theirItem = theirItem;
    }

    protected Offer(Parcel in) {
        myItem = (TradeItem) in.readValue(TradeItem.class.getClassLoader());
        theirItem = (TradeItem) in.readValue(TradeItem.class.getClassLoader());
        arrayPosition = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(myItem);
        dest.writeValue(theirItem);
        dest.writeInt(arrayPosition);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Offer> CREATOR = new Parcelable.Creator<Offer>() {
        @Override
        public Offer createFromParcel(Parcel in) {
            return new Offer(in);
        }

        @Override
        public Offer[] newArray(int size) {
            return new Offer[size];
        }
    };
}
