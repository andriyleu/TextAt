package com.andriy.textat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class MarkListActivity extends AppCompatActivity
        implements MapHandler.OnFragmentInteractionListener, LoaderManager.LoaderCallbacks<Cursor> {

    private ListView markList;
    private Toolbar toolbar;
    private LinearLayout mapList;
    private ArrayList<Mark> marks;
    private MapHandler mapHandler;
    private ClusterManager<Mark> clusterManager;
    private boolean isList = true;
    private LatLngBounds bounds;
    private GoogleMap map;
    private Menu menu;
    private MenuItem toggleButton;
    private LatLngBounds.Builder builder = LatLngBounds.builder();


    private Map<String, Marker> markers;

    // This is the Adapter being used to display the list's data
    SimpleCursorAdapter mAdapter;


    private void bindElements() {
        markList = findViewById(R.id.list);
        toolbar = findViewById(R.id.listToolbar);
        mapList = findViewById(R.id.map_layout);
        mapHandler = (MapHandler) this.getSupportFragmentManager().findFragmentById(R.id.mapList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_list);

        bindElements();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        marks = intent.getParcelableArrayListExtra("marks");
        String title = intent.getStringExtra("title");

        getSupportActionBar().setTitle(title);

        // Populate List
        CustomListAdapter adapter = new CustomListAdapter(marks, this);
        markList.setAdapter(adapter);
        markList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                showMapList();

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marks.get(i).getPosition(), 90);
                mapHandler.getMap().animateCamera(cameraUpdate);
            }
        });


    }

    @SuppressLint("MissingPermission")
    public void setMarks() {

        for (Mark m : marks) {
            mapHandler.getmClusterManager().addItem(m);
            builder.include(m.getPosition());
        }

        // mapHandler.getmClusterManager().cluster();
        mapHandler.getMap().setMyLocationEnabled(false);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        super.onPrepareOptionsMenu(m);


        getMenuInflater().inflate(R.menu.menu_list, m);
        menu = m;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggleListMap:
                if (isList) {
                    setCameraOnToggle();
                    showMapList();
                } else {
                    showMarkList();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMarkList() {
        isList = true;
        menu.getItem(0).setIcon(R.drawable.ic_map);
        markList.setVisibility(View.VISIBLE);
        mapList.setVisibility(View.GONE);
    }

    private void showMapList() {
        isList = false;
        markList.setVisibility(View.GONE);
        mapList.setVisibility(View.VISIBLE);
        menu.getItem(0).setIcon(R.drawable.ic_list);
    }

    private void setCameraOnToggle() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.15);

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, padding);

        mapHandler.getMap().animateCamera(cu);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }
}
