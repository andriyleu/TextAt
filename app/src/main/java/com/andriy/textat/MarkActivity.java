package com.andriy.textat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class MarkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);

        // Go back

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        // Get all elements
        TextView createdBy = findViewById(R.id.createdBy);
        TextView coordinates = findViewById(R.id.coordinates);
        TextView description = findViewById(R.id.description);
        TextView rating = findViewById(R.id.rating);
        TextView timestamp = findViewById(R.id.timestamp);

        // Get info from extras
        Intent i = getIntent();
        Mark mark = (Mark) i.getExtras().getParcelable("mark");

        createdBy.setText("Anotación creada por: " + mark.getUser());
        coordinates.setText("(" + mark.getLocation().getLatitude() +", " + mark.getLocation().getLongitude() +")");
        description.setText(mark.getDescription());
        rating.setText(Integer.toString(mark.getRating()));
        timestamp.setText(mark.getTimestamp().toDate().toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
