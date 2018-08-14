package com.andriy.textat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MarkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark);

        // Get all elements
        TextView createdBy = findViewById(R.id.createdBy);
        TextView coordinates = findViewById(R.id.coordinates);
        TextView description = findViewById(R.id.description);
        TextView rating = findViewById(R.id.rating);
        TextView timestamp = findViewById(R.id.timestamp);

        // Get info from extras
        Intent i = getIntent();
        Mark mark = (Mark) i.getExtras().getSerializable("mark");

        createdBy.setText("Anotación creada por: " + mark.getUser());
        coordinates.setText("(" + mark.getLocation().getLatitude() +", " + mark.getLocation().getLongitude() +")");
        description.setText(mark.getDescription());
        rating.setText(Integer.toString(mark.getRating()));
        timestamp.setText(mark.getTimestamp().toString());

    }
}
