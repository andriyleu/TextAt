package com.andriy.textat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.ClusterManager.OnClusterItemInfoWindowClickListener;

import java.util.ArrayList;
import java.util.List;

public class MapHandler extends Fragment implements OnMapReadyCallback {

    // Map related objects
    private GoogleMap map;
    private ClusterManager<Mark> mClusterManager;

    // Interaction with parent activity
    private MainActivity parent;

    // Fragment interaction with parent, not used but needed
    private OnFragmentInteractionListener mListener;

    // Debug
    public static final String TAG = "MapHandler";



    public MapHandler() {
        // Required empty public constructor
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parent = ((MainActivity) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map_handler, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (map == null) {

            map = googleMap;
            mClusterManager = new ClusterManager<>(getActivity(), googleMap);

            getMap().setMyLocationEnabled(true);
            googleMap.setOnCameraIdleListener(mClusterManager);
            googleMap.setOnMarkerClickListener(mClusterManager);
            googleMap.setOnInfoWindowClickListener(mClusterManager);


            // CustomRenderer in order to decrease number of elements needed to start clustering
            CustomClusterRenderer renderer = new CustomClusterRenderer(getActivity(), map, mClusterManager, map.getCameraPosition().zoom, map.getMaxZoomLevel());
            getMap().setOnCameraMoveListener(renderer);

            getmClusterManager().setRenderer(renderer);

            getmClusterManager()
                    .setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Mark>() {
                        @Override
                        public boolean onClusterClick(final Cluster<Mark> cluster) {
                            LatLngBounds.Builder builder = LatLngBounds.builder();
                            for (ClusterItem item : cluster.getItems()) {
                                builder.include(item.getPosition());
                            }

                            final LatLngBounds bounds = builder.build();
                            getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                            return true;
                        }
                    });

            getmClusterManager().setOnClusterItemInfoWindowClickListener(new OnClusterItemInfoWindowClickListener<Mark>() {
                @Override
                public void onClusterItemInfoWindowClick(Mark mark) {
                    Intent intent = new Intent(getActivity(), MarkDetailActivity.class);
                    intent.putExtra("mark", mark);
                    intent.putExtra("id", mark.getId());
                    startActivity(intent);
                }


            });
        }
    }

    public ClusterManager<Mark> getmClusterManager() {
        return mClusterManager;
    }

    public GoogleMap getMap() {
        return map;
    }

    // Fragment interaction with Activity
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
