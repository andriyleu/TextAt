package com.andriy.textat;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.clustering.ClusterItem;

public class Mark implements Parcelable, ClusterItem {
    private GeoPoint location;
    private String title;
    private String description;
    private String uri;
    private String user;
    private Timestamp timestamp;
    private long privacy;
    private long rating;
    private long visibility;
    private boolean hasImages;
    private String id;

    // empty constructor needed to retrieve from Firebase (POJO)
    public Mark() {

    }

    public Mark(GeoPoint l, String d, String u, String c, Timestamp t, long r, long p, long v, boolean i) {
        location = l;
        description = d;
        uri = u;
        user = c;
        rating = r;
        timestamp = t;
        title = "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
        privacy = p;
        setVisibility(v);
        hasImages = i;
    }

    public Mark(Parcel in) {
        Location l = in.readParcelable(Location.class.getClassLoader());
        location = new GeoPoint(l.getLatitude(), l.getLongitude());
        description = in.readString();
        uri = in.readString();
        user = in.readString();
        rating = in.readLong();
        privacy = in.readLong();
        setVisibility(in.readLong());
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
        hasImages = (in.readInt() == 0) ? false : true;
        id = in.readString();
        title = in.readString();
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
        parcel.writeLong(rating);
        parcel.writeLong(privacy);
        parcel.writeLong(getVisibility());
        parcel.writeParcelable(timestamp, i);
        parcel.writeInt(hasImages ? 1 : 0);
        parcel.writeString(id);
        parcel.writeString(getTitle());
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

    public long getRating() {
        return rating;
    }

    public String getUser() {
        return user;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public long getPrivacy() {
        return privacy;
    }

    public long getVisibility() {
        return visibility;
    }

    public boolean isHasImages() {
        return hasImages;
    }

    @Exclude
    @Override
    public LatLng getPosition() {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Exclude
    @Override
    public String getTitle() {
        return "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    @Exclude
    @Override
    public String getSnippet() {
        return description;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Mark)) return false;
        return id.equals(((Mark) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public void setVisibility(long visibility) {
        this.visibility = visibility;
    }
}