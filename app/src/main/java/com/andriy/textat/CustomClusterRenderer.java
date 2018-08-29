package com.andriy.textat;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.OverridingMethodsMustInvokeSuper;

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

    @Override
    protected void onClusterRendered(Cluster<Mark> cluster, Marker marker) {
        super.onClusterRendered(cluster, marker);
    }

    @Override
    protected void onClusterItemRendered(Mark mark, Marker marker) {
        super.onClusterItemRendered(mark, marker);
    }
}
