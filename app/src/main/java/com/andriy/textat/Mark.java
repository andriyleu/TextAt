package com.andriy.textat;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;


import java.util.Date;
import java.util.HashMap;

public class Mark implements Parcelable {
    private final Location location;
    private final String description;
    private final String url;
    private final String user;
    private final Date timestamp;
    private final int rating;


    public Mark(Location l, String d, String u, String c, Date t, int r) {
        location = l;
        description = d;
        url = u;
        user = c;
        rating = r;
        timestamp = t;
    }

    public Mark(Parcel in) {
        location = in.readParcelable(Location.class.getClassLoader());
        description = in.readString();
        url = in.readString();
        user = in.readString();
        timestamp = new Date(in.readLong());
        rating = in.readInt();
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
        HashMap<String, Object> hashed = new HashMap<>();

        hashed.put("location", getLocation());
        hashed.put("description", getDescription());
        hashed.put("url", getUrl());
        hashed.put("rating", getRating());

        return hashed;
    }

    public Location getLocation() {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(location, i);
        parcel.writeString(description);
        parcel.writeString(url);
        parcel.writeString(user);
        parcel.writeLong(timestamp.getTime());
        parcel.writeInt(rating);

    }

    public String getUser() {
        return user;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}