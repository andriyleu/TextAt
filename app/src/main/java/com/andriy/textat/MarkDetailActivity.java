package com.andriy.textat;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class MarkDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView createdBy;
    private TextView description;
    private TextView rating;
    private TextView timestamp;
    private ImageView image1;
    private ImageView image2;
    private ImageView image3;
    private Mark m;

    private String id;
    FirebaseStorage storage;
    String imageURL;

    private boolean zoomOut =  false;



    StorageReference storageRef;


    private void bindElements() {
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbar = findViewById(R.id.toolbar_layout);
        createdBy = findViewById(R.id.createdBy);
        description = findViewById(R.id.description);
        rating = findViewById(R.id.rating);
        timestamp = findViewById(R.id.timestamp);
        image1 = findViewById(R.id.markImage1);
        image2 = findViewById(R.id.markImage2);
        image3 = findViewById(R.id.markImage3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_detail);

        bindElements();

        //base
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Toolbar setup
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.toolbar_title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });



        // image setup

        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        // Get info from extras
        Intent i = getIntent();
        Mark mark = (Mark) i.getExtras().getParcelable("mark");
        id = i.getExtras().getString("id");
        m = mark;

        createdBy.setText("Anotación creada por " + mark.getUser());
        getSupportActionBar().setTitle(mark.getTitle());
        description.setText(mark.getDescription());

        setImages();

        // rating setUp

        rating.setText(Long.toString(mark.getRating()));
        timestamp.setText(mark.getTimestamp().toDate().toString());
    }

    private void setImages() {


        final StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + id + "/" + "1.jpg");

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageURL = uri.toString();
                Glide.with(getApplicationContext()).load(imageURL).into(image1);
                image1.setVisibility(View.VISIBLE);
            }
        });



        final StorageReference ref2 = FirebaseStorage.getInstance().getReference().child("images/" + id + "/" + "2.jpg");

        ref2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageURL = uri.toString();
                Glide.with(getApplicationContext()).load(imageURL).into(image2);
                image2.setVisibility(View.VISIBLE);
            }
        });

        final StorageReference ref3 = FirebaseStorage.getInstance().getReference().child("images/" + id + "/" + "2.jpg");

        ref3.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageURL = uri.toString();
                Glide.with(getApplicationContext()).load(imageURL).into(image3);
                image3.setVisibility(View.VISIBLE);

            }
        });

    }

}
