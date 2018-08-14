package com.andriy.textat;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.HashMap;

public class Mark implements Parcelable {
    private GeoPoint location;
    private String description;
    private String url;
    private String user;
    private Timestamp timestamp;
    private int rating;

    public Mark() {

    }


    public Mark(GeoPoint l, String d, String u, String c, Timestamp t, int r) {
        location = l;
        description = d;
        url = u;
        user = c;
        rating = r;
        timestamp = t;
    }

    public Mark(Parcel in) {
        Location l = in.readParcelable(Location.class.getClassLoader());
        location = new GeoPoint(l.getLatitude(), l.getLongitude()); // this works?
        description = in.readString();
        url = in.readString();
        user = in.readString();
        rating = in.readInt();
        timestamp = new Timestamp(new Date(in.readLong()));
    }

    public static final Creator<Mark> CREATOR = new Creator<Mark>() {
        @Override
        public Mark createFromParcel(Parcel in) {
            return new Mark(in);
        }

        @Override
        public Mark[] newArray(int size) {
            return new Mark[size];
        }
    };

    protected HashMap<String, Object> toHashMap() {

        // #TODO complete hashing

        HashMap<String, Object> hashed = new HashMap<>();

        hashed.put("location", location);
        hashed.put("description", description);
        hashed.put("url", url);
        hashed.put("rating", rating);

        return hashed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        Location l = new Location(""); // provider not needed
        l.setLatitude(location.getLatitude());
        l.setLongitude(location.getLongitude());

        parcel.writeParcelable(l, i);
        parcel.writeString(description);
        parcel.writeString(url);
        parcel.writeString(user);
        parcel.writeLong(timestamp.toDate().getTime());
        parcel.writeInt(rating);

    }


    public GeoPoint getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public int getRating() {
        return rating;
    }

    public String getUser() {
        return user;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }


}