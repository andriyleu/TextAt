package com.andriy.textat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
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

    private GoogleMap map;
    private LocationManager locationManager;

    private Location currentLocation;

    private ListenerRegistration updateListener;
    public static final String TAG = "debug";

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ClusterManager<Mark> mClusterManager;


    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 100;

    private OnFragmentInteractionListener mListener;

    private MainActivity parent;

    List<Mark> marksToShow = new ArrayList<Mark>();

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
    public void onDestroy() {
        super.onDestroy();
        updateListener.remove();
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
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            updateCamera(location);
                            processPendingDistanceMarks();
                        }

                        @Override
                        public void onStatusChanged(String s, int i, Bundle bundle) {

                        }

                        @Override
                        public void onProviderEnabled(String s) {
                        }

                        @Override
                        public void onProviderDisabled(String s) {

                        }
                    }
            );


            map = googleMap;
            map.setMyLocationEnabled(true);

            mClusterManager = new ClusterManager<>(getActivity(), map);

            updateListener = db.collection("anotaciones")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w("TAG", "listen:error", e);
                                return;
                            }

                            for (DocumentChange document : snapshots.getDocumentChanges()) {
                                Log.w("TAG", "Añadiendo "+document.getDocument().getId(), e);
                                DocumentSnapshot d = document.getDocument();
                                Mark m = document.getDocument().toObject(Mark.class);
                                m.setId(d.getId());
                                switch (document.getType()) {
                                    case ADDED:

                                        // only user's private marks
                                        if (m.getPrivacy() == 1 && !m.getUser().equals(parent.getUser().getEmail())) {
                                            continue;
                                        }

                                        if (m.getPrivacy() == 2) {
                                            if (currentLocation == null) {
                                                marksToShow.add(m);
                                            } else {
                                                if (!isMarkVisible(m, currentLocation))
                                                    continue;
                                            }
                                        }

                                        break;

                                    case MODIFIED:
                                        break;

                                    case REMOVED:
                                        mClusterManager.removeItem(m);
                                }
                            }
                            mClusterManager.cluster();
                        }
                    });
            googleMap.setOnCameraIdleListener(mClusterManager);
            googleMap.setOnMarkerClickListener(mClusterManager);
            googleMap.setOnInfoWindowClickListener(mClusterManager);

            CustomClusterRenderer renderer = new CustomClusterRenderer(getActivity(), map, mClusterManager, map.getCameraPosition().zoom, map.getMaxZoomLevel());
            map.setOnCameraMoveListener(renderer);

            mClusterManager.setRenderer(renderer);


            mClusterManager
                    .setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Mark>() {
                        @Override
                        public boolean onClusterClick(final Cluster<Mark> cluster) {
                            LatLngBounds.Builder builder = LatLngBounds.builder();
                            for (ClusterItem item : cluster.getItems()) {
                                builder.include(item.getPosition());
                            }

                            final LatLngBounds bounds = builder.build();
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                            return true;
                        }
                    });

            mClusterManager.setOnClusterItemInfoWindowClickListener(new OnClusterItemInfoWindowClickListener<Mark>() {
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

    private static boolean isMarkVisible(Mark mark, Location currentLocation) {
        GeoPoint l = mark.getLocation();
        Location markLocation = new Location("");
        markLocation.setLongitude(l.getLongitude());
        markLocation.setLatitude(l.getLatitude());

        return markLocation.distanceTo(currentLocation) <= mark.getVisibility();
    }

    private void processPendingDistanceMarks() {
        for (Mark mark: marksToShow) {
            if (!isMarkVisible(mark, currentLocation))
                    continue;

            mClusterManager.addItem(mark);
        }
        mClusterManager.cluster();
        marksToShow.clear();
    }

    protected void updateCamera(Location location) {
        if (location != null) {
            currentLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
            map.animateCamera(cameraUpdate);
        }
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    // Fragment interaction with Activity
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
