package com.andriy.textat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Query;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.AutoLinkOnClickListener;
import com.luseen.autolinklibrary.AutoLinkTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MarkDetailActivity extends AppCompatActivity  implements CompletionHandler{

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView createdBy;
    private AutoLinkTextView description;
    private TextView rating;
    private TextView timestamp;
    private TextView uri;
    private TextView markId;
    private TextView visibility;
    private List<ImageView> images;
    private LinearLayout imageLayout;
    private View separator;
    private Mark m;

    private Uri imageLink;

    private FirebaseAuth mAuth;

    private String idDocument;
    FirebaseStorage storage;
    String imageURL;

    private boolean zoomOut = false;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageRef;
    SearchHandler searchHandler;


    private void bindElements() {
        images = new ArrayList<>();
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbar = findViewById(R.id.toolbar_layout);
        createdBy = findViewById(R.id.createdBy);
        description = findViewById(R.id.description);
        rating = findViewById(R.id.rating);
        timestamp = findViewById(R.id.timestamp);
        uri = findViewById(R.id.uri);
        markId = findViewById(R.id.markId);
        visibility = findViewById(R.id.visibility);
        images.add((ImageView) findViewById(R.id.markImage1));
        images.add((ImageView) findViewById(R.id.markImage2));
        images.add((ImageView) findViewById(R.id.markImage3));
        imageLayout = findViewById(R.id.imageViewer);
        separator = findViewById(R.id.imagesSeparator);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_detail);

        searchHandler = new SearchHandler();

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

                if (autoLinkMode == AutoLinkMode.MODE_HASHTAG) {
                    searchHandler.getIndex().searchAsync(new Query(matchedText), MarkDetailActivity.this);
                }
            }
        });

        // Get info from extras
        Intent i = getIntent();
        Mark mark = (Mark) i.getExtras().getParcelable("mark");
        idDocument = i.getExtras().getString("id");
        m = mark;

        // mark id
        markId.setText("ID de la anotación: " + m.getId());
        markId.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("id", m.getId());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MarkDetailActivity.this, "Se ha copiado el ID de la anotación al portapapeles",
                        Toast.LENGTH_LONG).show();
            }
        });

        // visibility
        if (m.getPrivacy() == 2) {
            visibility.setText("Esta anotación es visible sólo a " + m.getVisibility() + "m.");
        }


        createdBy.setText("Anotación creada por " + mark.getUser());
        getSupportActionBar().setTitle(mark.getTitle());
        description.setAutoLinkText(mark.getDescription());

        setImages();

        // rating setUp
        String markRating = Long.toString(mark.getRating());
        if (mark.getRating() == 1) {
            markRating = "+" + markRating;
        }
        rating.setText(markRating);

        //timestamp to spanish
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy");

        timestamp.setText(sdf.format(mark.getTimestamp().toDate()));

        // uri

        if (!m.getUri().isEmpty()) {
            uri.setVisibility(View.VISIBLE);
            uri.setText(Html.fromHtml("<a href=" + m.getUri().toString() + "> URI"));
            uri.setMovementMethod(LinkMovementMethod.getInstance());

            uri.setOnClickListener(new View.OnClickListener() {
                                       public void onClick(View v) {
                                           Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                           browserIntent.setData(Uri.parse(m.getUri().toString()));
                                           startActivity(browserIntent);
                                       }
                                   }
            );
        }

        // images setUp
        if (m.isHasImages()) {
            separator.setVisibility(View.VISIBLE);
            imageLayout.setVisibility(View.VISIBLE);

            // Get images from Storage
            setImages();

        }
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
            db.collection("anotaciones").document(m.getId())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            searchHandler.removeMark(m);
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
        int imageNumber = 1;
        for (final ImageView image : images) {
            final StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + m.getId() + "/" + Integer.toString(imageNumber++) + ".jpg");

            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    imageLink = uri;
                    imageURL = uri.toString();
                    Glide.with(getApplicationContext()).load(imageURL).into(image);
                    image.setVisibility(View.VISIBLE);

                    image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(imageLink, "image/*");
                            startActivity(intent);
                        }
                    });
                }
            });
        }
    }

    @Override
    public void requestCompleted(JSONObject jsonObject, AlgoliaException e) {
        JSONArray hits  = null;

        try {
            ArrayList<Mark> searchMarks = new ArrayList<>();
            hits = jsonObject.getJSONArray("hits");
            for (int i = 0; i < hits.length(); i++) {
                searchMarks.add(new Mark(hits.getJSONObject(i)));
            }

            if (searchMarks.isEmpty())
                return;

            Intent intent = new Intent(MarkDetailActivity.this, MarkListActivity.class);
            intent.putParcelableArrayListExtra("marks", searchMarks);
            startActivity(intent);

        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }
}
