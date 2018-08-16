package com.andriy.textat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;


public class MarkDetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)
                findViewById(R.id.toolbar_layout);
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.toolbar_title);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        // Get all elements
        TextView createdBy = findViewById(R.id.createdBy);
        TextView description = findViewById(R.id.description);
        TextView rating = findViewById(R.id.rating);
        TextView timestamp = findViewById(R.id.timestamp);

        // Get info from extras
        Intent i = getIntent();
        Mark mark = (Mark) i.getExtras().getParcelable("mark");

        createdBy.setText("Anotación creada por " + mark.getUser());
        getSupportActionBar().setTitle("(" + mark.getLocation().getLatitude() +", " + mark.getLocation().getLongitude() +")");
        description.setText(mark.getDescription());

        rating.setText("+1");
        // rating.setText(Integer.toString(mark.getRating()));
        timestamp.setText(mark.getTimestamp().toDate().toString());
    }
}
