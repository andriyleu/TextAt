package com.andriy.textat;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.AutoLinkOnClickListener;
import com.luseen.autolinklibrary.AutoLinkTextView;


public class MarkDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView createdBy;
    private AutoLinkTextView description;
    private TextView rating;
    private TextView timestamp;
    private ImageView image1;
    private ImageView image2;
    private ImageView image3;
    private Mark m;

    private FirebaseAuth mAuth;

    private String idDocument;
    FirebaseStorage storage;
    String imageURL;

    private boolean zoomOut =  false;

    FirebaseFirestore db = FirebaseFirestore.getInstance();


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

        // desc

        description.addAutoLinkMode(
                AutoLinkMode.MODE_HASHTAG, AutoLinkMode.MODE_MENTION);

        description.setMentionModeColor(ContextCompat.getColor(this, R.color.colorAccent));
        description.setHashtagModeColor(ContextCompat.getColor(this, R.color.colorAccent));


        description.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
            @Override
            public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                if (autoLinkMode == AutoLinkMode.MODE_MENTION) {

                }
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
        idDocument = i.getExtras().getString("id");
        m = mark;

        createdBy.setText("Anotación creada por " + mark.getUser());
        getSupportActionBar().setTitle(mark.getTitle());
        description.setAutoLinkText(mark.getDescription());

        setImages();

        // rating setUp

        rating.setText(Long.toString(mark.getRating()));
        timestamp.setText(mark.getTimestamp().toDate().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mAuth = FirebaseAuth.getInstance();
        String email = mAuth.getCurrentUser().getEmail();
        if (email.equals(m.getUser())) {
            getMenuInflater().inflate(R.menu.menu_mark_detail, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.deleteMark) {
            db.collection("anotaciones").document(idDocument)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
        }

        return super.onOptionsItemSelected(item);
    }


    private void setImages() {


        final StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + idDocument + "/" + "1.jpg");

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageURL = uri.toString();
                Glide.with(getApplicationContext()).load(imageURL).into(image1);
                image1.setVisibility(View.VISIBLE);
            }
        });



        final StorageReference ref2 = FirebaseStorage.getInstance().getReference().child("images/" + idDocument + "/" + "2.jpg");

        ref2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                imageURL = uri.toString();
                Glide.with(getApplicationContext()).load(imageURL).into(image2);
                image2.setVisibility(View.VISIBLE);
            }
        });

        final StorageReference ref3 = FirebaseStorage.getInstance().getReference().child("images/" + idDocument + "/" + "2.jpg");

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
