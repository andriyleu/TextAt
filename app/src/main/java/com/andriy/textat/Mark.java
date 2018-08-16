package com.andriy.textat;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Date;

public class Mark implements Parcelable, ClusterItem {
    private GeoPoint location;
    private String title;
    private String description;
    private String uri;
    private String user;
    private Timestamp timestamp;
    private int privacy;
    private int rating;
    private int visibility;

    // empty constructor needed to retrieve from Firebase (POJO)
    public Mark() {

    }

    public Mark(GeoPoint l, String d, String u, String c, Timestamp t, int r, int p, int v) {
        location = l;
        description = d;
        uri = u;
        user = c;
        rating = r;
        timestamp = t;
        title = "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
        privacy = p;
        visibility = v;
    }

    public Mark(Parcel in) {
        Location l = in.readParcelable(Location.class.getClassLoader());
        location = new GeoPoint(l.getLatitude(), l.getLongitude());
        description = in.readString();
        uri = in.readString();
        user = in.readString();
        rating = in.readInt();
        privacy = in.readInt();
        timestamp = new Timestamp(new Date(in.readLong()));
        title = "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
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
        parcel.writeString(uri);
        parcel.writeString(user);
        parcel.writeLong(timestamp.toDate().getTime());
        parcel.writeInt(rating);
        parcel.writeInt(privacy);
        parcel.writeInt(visibility);
        parcel.writeString(title);
    }


    public GeoPoint getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getUri() {
        return uri;
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

    public int getPrivacy() {
        return privacy;
    }

    @Exclude
    @Override
    public LatLng getPosition() {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Exclude
    @Override
    public String getTitle() {
        return title;
    }

    @Exclude
    @Override
    public String getSnippet() {
        return description;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }
}