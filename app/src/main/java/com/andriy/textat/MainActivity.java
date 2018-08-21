package com.andriy.textat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MapHandler.OnFragmentInteractionListener {

    // UI Activity elements
    private MapHandler mapHandler;
    private TextView loc;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private ListenerRegistration updateListener;

    // Algolia
    Client client = new Client("KAJVMYN673", "5d42104707798a805f13ff8658a595dc");
    Index index;
    SearchHandler searchHandler;

    // Internal data handling
    private Map<String, Mark> marks;
    private List<Mark> nearbyMarks;

    // Location provider
    private final int REQUEST_FINE_LOCATION = 1;
    private Location currentLocation;
    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private boolean boot = false;

    // Debug related
    public static final String TAG = "MainActivity";

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void bindElements() {
        loc = findViewById(R.id.showLocation);
        mapHandler = (MapHandler) this.getSupportFragmentManager().findFragmentById(R.id.map);
        toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind XML to Objects
        bindElements();

        // Get user info from LoginActivity
        Intent i = getIntent();
        user = i.getExtras().getParcelable("user");

        // Set-up data structures to handle mark's info
        marks = new HashMap<>();
        nearbyMarks = new ArrayList<>();

        // Check if user has given app location permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermissions();
        }

        startLocationUpdates();

        // Algolia
        index = client.getIndex("anotaciones");


        // Set up listener that parses initially everything and then receive updates
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
                            Log.w("TAG", "Añadiendo " + document.getDocument().getId(), e);
                            DocumentSnapshot d = document.getDocument();
                            String id = d.getId();
                            Mark m = document.getDocument().toObject(Mark.class);
                            m.setId(id);
                            switch (document.getType()) {
                                case ADDED:

                                    // Case: Private marks
                                    if (m.getPrivacy() == 1 && !m.getUser().equals(user.getEmail())) {
                                        continue;
                                    }

                                    // Case: Nearby marks
                                    if (m.getPrivacy() == 2) {
                                        nearbyMarks.add(m);
                                        if (currentLocation != null) {
                                            if (isMarkVisible(m, currentLocation)) {
                                                mapHandler.getmClusterManager().addItem(m);
                                                continue;
                                            }
                                        }
                                    }

                                    mapHandler.getmClusterManager().addItem(m);
                                    marks.put(id, m);
                                    break;

                                case MODIFIED:
                                    Iterator<Mark> i1 = nearbyMarks.iterator();
                                    while (i1.hasNext()) {
                                        Mark mark = i1.next();
                                        if (id.equals(mark.getId())) {
                                            mark.setVisibility(m.getVisibility());
                                        }
                                    }
                                    break;

                                case REMOVED:
                                    mapHandler.getmClusterManager().removeItem(marks.get(id));
                                    marks.remove(id);
                                    Iterator<Mark> i2 = nearbyMarks.iterator();
                                    while (i2.hasNext()) {
                                        Mark mark = i2.next();
                                        if (mark.getId() == id) {
                                            nearbyMarks.remove(i2);
                                        }
                                    }
                            }
                        }
                        mapHandler.getmClusterManager().cluster();
                    }
                });


        // Rest of UI set up
        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddMarkActivity.class);
                intent.putExtra("location", currentLocation);
                startActivity(intent);
            }
        });

        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    public void updateNearbyMarks() {

        if (!nearbyMarks.isEmpty()) {
            for (Mark mark : nearbyMarks) {
                if (!isMarkVisible(mark, currentLocation)) {
                    mapHandler.getmClusterManager().removeItem(mark); // If mark isn't visible anymore remove it from cluster
                    continue;
                } else {
                    mapHandler.getmClusterManager().addItem(mark); // If mark becomes visible add it to cluster
                }
            }
            mapHandler.getmClusterManager().cluster(); // Re-cluster
        }
    }

    public void updateCamera(Location location) {
        if (location != null) {
            currentLocation = location;
            loc.setText("(" + location.getLatitude() + ", " + location.getLongitude() + ")");

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
            mapHandler.getMap().animateCamera(cameraUpdate);
        }
    }

    private static boolean isMarkVisible(Mark mark, Location currentLocation) {
        GeoPoint l = mark.getLocation();
        Location markLocation = new Location("");
        markLocation.setLongitude(l.getLongitude());
        markLocation.setLatitude(l.getLatitude());

        return markLocation.distanceTo(currentLocation) <= mark.getVisibility();
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        if (!boot) {
                            boot = true;
                            updateCamera(locationResult.getLastLocation());
                        }

                        updateNearbyMarks();
                    }

                },
                Looper.myLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        /*MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView =
                (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.myMarks) {
            handleMyMarks();

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

            Intent intent = new Intent(this, MarkDetailActivity.class);
            startActivity(intent);


        } else if (id == R.id.nav_send) {
            // Disconnect
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleMyMarks() {
        CompletionHandler completionHandler = new CompletionHandler() {
            @Override
            public void requestCompleted(JSONObject content, AlgoliaException error) {

                ArrayList<Mark> marksList = new ArrayList<>();

                try {
                    JSONArray hits  = content.getJSONArray("hits");
                    for (int i = 0; i < hits.length(); i++) {
                        JSONObject jsonObject = hits.getJSONObject(i);
                        String id = jsonObject.getString("objectID");
                        marksList.add(marks.get(id));
                    }

                    Intent intent = new Intent(MainActivity.this, MarkListActivity.class);
                    intent.putParcelableArrayListExtra("marks", marksList);
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        };

        index.searchAsync(new Query(user.getEmail()), completionHandler);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permisos aceptados!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Esta aplicación no puede funcionar sin localización!", Toast.LENGTH_SHORT).show();
                    getLocationPermissions();
                }
        }
    }


    private void getLocationPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }

    public MapHandler getMapHandler() {
        return mapHandler;
    }

    public FirebaseUser getUser() {
        return user;
    }

    public Map<String, Mark> getMarks() {
        return marks;
    }

    public List<Mark> getNearbyMarks() {
        return nearbyMarks;
    }
}
