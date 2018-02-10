package com.carmichael.swip.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by carte on 7/19/2017.
 */

public class TradeItem implements Parcelable {

    public String itemId;
    public String name;
    public String description;
    public HashMap<String, Object> offers;
    public HashMap<String, Object> matches;
    public String userId;
    public String outMarket;
    public String tradeComplete;

    public TradeItem(){}

    protected TradeItem(Parcel in) {
        itemId = in.readString();
        name = in.readString();
        description = in.readString();
        userId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemId);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(userId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TradeItem> CREATOR = new Creator<TradeItem>() {
        @Override
        public TradeItem createFromParcel(Parcel in) {
            return new TradeItem(in);
        }

        @Override
        public TradeItem[] newArray(int size) {
            return new TradeItem[size];
        }
    };

    public ArrayList<String> getHashMapKeysAsStrings(HashMap<String, Object> hashMap){
        if(hashMap != null){
            Set<String> myOffers = hashMap.keySet();
            ArrayList<String> itemKeyStrings = new ArrayList<>();

            for(String s : myOffers){
                itemKeyStrings.add(s);
            }

            return itemKeyStrings;
        }

        ArrayList<String> itemKeyStrings = new ArrayList<>();
        return itemKeyStrings;
    }


    public String getTradeComplete() {
        return tradeComplete;
    }

    public void setTradeComplete(String tradeComplete) {
        this.tradeComplete = tradeComplete;
    }

    public String getOutMarket() {
        return outMarket;
    }

    public void setOutMarket(String outMarket) {
        this.outMarket = outMarket;
    }

    public HashMap<String, Object> getMatches() {
        return matches;
    }

    public void setMatches(HashMap<String, Object> matches) {
        this.matches = matches;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Object> getOffers() {
        return offers;
    }

    public void setOffers(HashMap<String, Object> offers) {
        this.offers = offers;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "TradeItem{" +
                "itemId='" + itemId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", offers=" + offers +
                ", matches=" + matches +
                ", userId='" + userId + '\'' +
                ", outMarket='" + outMarket + '\'' +
                ", tradeComplete='" + tradeComplete + '\'' +
                '}';
    }
}