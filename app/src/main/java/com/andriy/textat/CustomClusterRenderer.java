package com.andriy.textat;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class CustomClusterRenderer extends DefaultClusterRenderer<Mark> implements GoogleMap.OnCameraMoveListener {

    private GoogleMap mMap;
    private float currentZoomLevel, maxZoomLevel;


    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<Mark> clusterManager, float currentZoom, float maxZoom) {
        super(context, map, clusterManager);
        setMinClusterSize(3);
        mMap = map;
        currentZoomLevel = currentZoom;
        maxZoomLevel = maxZoom;

    }

    @Override
    public void onCameraMove() {
        currentZoomLevel = mMap.getCameraPosition().zoom;
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {

        return currentZoomLevel + 2 < maxZoomLevel && cluster.getSize() > 3;

    }
}
