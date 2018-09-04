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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.instantsearch.model.AlgoliaResultsListener;
import com.algolia.instantsearch.model.SearchResults;
import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.CompletionHandler;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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

import java.util.ArrayList;
import java.util.List;

public class MarkDetailActivity extends AppCompatActivity implements CompletionHandler, AlgoliaResultsListener {

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView createdBy;
    private AutoLinkTextView description;
    private ImageView rating;
    private TextView timestamp;
    private ImageView avatar;
    private TextView markId;
    private ImageView visibility;
    private TextView coordinates;
    private List<ImageView> images;
    private LinearLayout imageLayout;
    private View separator;
    private Mark m;
    private ImageView uri;
    private TextView visib;

    private Uri imageLink;

    private FirebaseAuth mAuth;
    private String user;

    private String idDocument;
    FirebaseStorage storage;
    String imageURL;

    private boolean zoomOut = false;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageRef;
    SearchHandler searchHandler;

    private String searching;


    private void bindElements() {
        images = new ArrayList<>();
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbar = findViewById(R.id.toolbar_layout);
        createdBy = findViewById(R.id.createdBy);
        description = findViewById(R.id.description);
        rating = findViewById(R.id.rating);
        timestamp = findViewById(R.id.timestamp);
        avatar = findViewById(R.id.userAvatar);
        markId = findViewById(R.id.markId);
        visibility = findViewById(R.id.visibility);
        images.add((ImageView) findViewById(R.id.markImage1));
        images.add((ImageView) findViewById(R.id.markImage2));
        images.add((ImageView) findViewById(R.id.markImage3));
        imageLayout = findViewById(R.id.imageViewer);
        separator = findViewById(R.id.imagesSeparator);
        coordinates = findViewById(R.id.coordinates);
        uri = findViewById(R.id.uri);
        visib = findViewById(R.id.visib);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_detail);

        bindElements();

        mAuth = FirebaseAuth.getInstance();


        // Get info from extras
        Intent i = getIntent();
        Mark mark = (Mark) i.getExtras().getParcelable("mark");
        idDocument = i.getExtras().getString("id");
        m = mark;
        user = ((TextAt) getApplication()).getUsernick();


        // Firebase
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();


        // Algolia
        searchHandler = new SearchHandler();

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

        // Description set up w/ onClicks on # (hashtags) and @ (mentions)
        description.addAutoLinkMode(
                AutoLinkMode.MODE_HASHTAG, AutoLinkMode.MODE_MENTION, AutoLinkMode.MODE_EMAIL);

        description.setMentionModeColor(ContextCompat.getColor(this, R.color.colorAccent));
        description.setHashtagModeColor(ContextCompat.getColor(this, R.color.colorAccent));


        description.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
            @Override
            public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {

                if (autoLinkMode == AutoLinkMode.MODE_MENTION) {
                    searching = matchedText.substring(2, matchedText.length());

                    searchHandler.getIndex().getObjectAsync(searching, new CompletionHandler() {
                                @Override
                                public void requestCompleted(JSONObject jsonObject, AlgoliaException e) {

                                    if (e != null) {
                                            searchHandler.getIndex().searchAsync(searchHandler.getUserMarks(searching, false), MarkDetailActivity.this);
                                            return;
                                    }

                                    Intent intent = new Intent(MarkDetailActivity.this, MarkDetailActivity.class);
                                    intent.putExtra("mark", new Mark(jsonObject));
                                    startActivity(intent);
                                }
                            }
                    );
                }

                if (autoLinkMode == AutoLinkMode.MODE_HASHTAG) {
                    searching = matchedText.substring(1, matchedText.length());
                    searchHandler.getIndex().searchAsync(searchHandler.getHashtag(matchedText), MarkDetailActivity.this);
                }
            }
        });

        description.setAutoLinkText(mark.getDescription());

        // Mark id allowing user to copy on click
        markId.setText("ID: " + m.getId());
        markId.setOnClickListener(new View.OnClickListener()

        {
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("id", m.getId());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MarkDetailActivity.this, "Se ha copiado el ID de la anotación al portapapeles",
                        Toast.LENGTH_LONG).show();
            }
        });

        // Set visibility if needed
        int privacy = (int) m.getPrivacy();
        switch (privacy) {
            case 0:
                visibility.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.public_mark));
                break;
            case 1:
                visibility.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.private_mark));
                break;
            case 2:
                visibility.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.near_mark));
                visib.setVisibility(View.VISIBLE);
                visib.setText("Anotación visible a ".concat(Long.toString(m.getVisibility())).concat("m"));
                break;
        }


        // Title and subtitle (Mark coordinates and user)
        createdBy.setText("Creado por @" + mark.getUser());
        getSupportActionBar().setTitle(mark.getTitle());

        // Set up rating
        int r = (int) m.getRating();

        switch (r) {
            case -1:
                rating.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dislike_mark));
                break;
            case 0:
                rating.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.neutral_mark));
                break;
            case 1:
                rating.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.like_mark));
                break;
        }

        //timestamp to spanish
        timestamp.setText("Publicado " + m.getDate());

        coordinates.setText("Lat: ".concat(Double.toString(m.getLocation().getLatitude()) + " Lon: ".concat(Double.toString(m.getLocation().getLongitude()))));

        // uri
        if (!m.getUri().isEmpty()) {
            uri.setVisibility(View.VISIBLE);

            uri.setOnClickListener(new View.OnClickListener() {
                                       public void onClick(View v) {
                                           Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                           browserIntent.setData(Uri.parse(m.getUri().toString()));
                                           startActivity(browserIntent);
                                       }
                                   }
            );
        }

        final StorageReference ref = FirebaseStorage.getInstance().getReference().child("users/".concat(m.getUser()).concat(".jpg"));
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri.toString()).apply(RequestOptions.circleCropTransform()).into(avatar);
            }
        });


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

        if (user.equals(m.getUser())) {
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
        JSONArray hits = null;

        try {
            ArrayList<Mark> searchMarks = new ArrayList<>();
            hits = jsonObject.getJSONArray("hits");
            for (int i = 0; i < hits.length(); i++) {
                searchMarks.add(new Mark(hits.getJSONObject(i)));
            }

            if (searchMarks.isEmpty()) // case used for when JSON is not properly formated
                return;

            Intent intent = new Intent(MarkDetailActivity.this, MarkListActivity.class);
            intent.putParcelableArrayListExtra("marks", searchMarks);
            intent.putExtra("title", "Anotaciones de: ".concat(searching));
            startActivity(intent);

        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onResults(@NonNull SearchResults results, boolean isLoadingMore) {

    }
}
