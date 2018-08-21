package com.andriy.textat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;

import java.io.IOException;


public class AddMarkActivity extends AppCompatActivity {

    // Database related stuff
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private SearchHandler searchHandler;

    public static final String TAG = "debug";

    // UI Elements
    private Toolbar toolbar;
    private Button publish;
    private TextView coordinates;
    private EditText description;
    private EditText uri;
    private Spinner privacySettings;
    private LinearLayout privacyOptions;
    private ImageView imageUpload1;
    private ImageView imageUpload2;
    private ImageView imageUpload3;


    // Logic elements
    int rating = 0;
    int selectedPrivacy = 0;
    int selectedVisibility = 0;

    // Image pickers
    public static final int PICK_IMAGE1 = 1; // image picker activity result
    public static final int PICK_IMAGE2 = 2;
    public static final int PICK_IMAGE3 = 3;

    private Uri image1;
    private Uri image2;
    private Uri image3;

    private boolean hasImages = false;

    private void bindElements() {
        toolbar = findViewById(R.id.toolbar);
        privacySettings = findViewById(R.id.privacySettings);
        description = findViewById(R.id.description);
        publish = findViewById(R.id.publishButton);
        uri = findViewById(R.id.uri);
        privacySettings = findViewById(R.id.privacySettings);
        privacyOptions = findViewById(R.id.privacyOptions);
        coordinates = findViewById(R.id.coordinates);
        imageUpload1 = findViewById(R.id.uploadImage1);
        imageUpload2 = findViewById(R.id.uploadImage2);
        imageUpload3 = findViewById(R.id.uploadImage3);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mark);

        searchHandler = new SearchHandler();

        // store
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // auth
        mAuth = FirebaseAuth.getInstance();


        bindElements();

        // Toolbar set-up and allow user to go back
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.addMarkTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        // Get user location
        Intent i = getIntent();
        final Location location = i.getParcelableExtra("location");

        // Set coordinates title
        coordinates.setText("(" + location.getLatitude() + ", " + location.getLongitude() + ")");

        // Privacy spinner

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.privacidad, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        privacySettings.setAdapter(adapter);


        privacySettings.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        selectedPrivacy = 0;
                        privacyOptions.setVisibility(View.GONE);
                        break;
                    case 1:
                        selectedPrivacy = 1;
                        privacyOptions.setVisibility(View.GONE);
                        break;
                    case 2:
                        selectedPrivacy = 2;
                        privacyOptions.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPrivacy = 0;
            }
        });

        // ImageUpload

        imageUpload1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePicker(PICK_IMAGE1);
            }
        });

        imageUpload2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePicker(PICK_IMAGE2);
            }
        });

        imageUpload3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePicker(PICK_IMAGE3);
            }
        });


        // Publish button

        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = uri.getText().toString();

                // Description is an obligatory field
                if (TextUtils.isEmpty(description.getText().toString())) {
                    description.setError("¡Tienes que escribir una descripción!");
                    return;
                }

                // URI field is optional, in case its filled check if it is a valid URL
                if (!TextUtils.isEmpty(url)) {
                    if (!Patterns.WEB_URL.matcher(url).matches()) {
                        uri.setError("¡Tienes que poner una url válida!");
                        return;
                    } else {
                        if (!url.contains("http://") && !url.contains("https://")) {
                            url = "https://" + url;
                        }
                    }
                }

                final Mark m = new Mark(new GeoPoint(location.getLatitude(), location.getLongitude()), description.getText().toString(), url, FirebaseAuth.getInstance().getCurrentUser().getEmail(), Timestamp.now(), rating, selectedPrivacy, selectedVisibility, hasImages);
                db.collection("anotaciones")
                        .add(m)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                String docRef = documentReference.getId();
                                m.setId(docRef);

                                try {
                                    searchHandler.addMark(m);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (image1 != null) {
                                    uploadImage(image1, docRef, 1);
                                }

                                if (image2 != null) {
                                    uploadImage(image2, docRef, 2);
                                }

                                if (image3 != null) {
                                    uploadImage(image3, docRef, 3);
                                }

                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
            }
        });
    }

    // Rating RadioGroup
    public void onRatingClick(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.negativeRating:
                if (checked)
                    rating = -1;
                break;
            case R.id.neutralRating:
                if (checked)
                    rating = 0;
                break;
            case R.id.positiveRating:
                if (checked)
                    rating = 1;
                break;
        }
    }

    // Visibility RadioGroup
    public void onVisibilityClick(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.closest:
                if (checked)
                    selectedVisibility = 50;
                break;
            case R.id.closer:
                if (checked)
                    selectedVisibility = 500;
                break;
            case R.id.close:
                if (checked)
                    selectedVisibility = 1000;
                break;
        }
    }

    public void imagePicker(int n) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, n);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && data != null) {
            Uri uri = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                hasImages = true;

                if (requestCode == PICK_IMAGE1) {
                    imageUpload1.setImageBitmap(bitmap);
                    image1 = uri;
                    return;
                }

                if (requestCode == PICK_IMAGE2) {
                    imageUpload2.setImageBitmap(bitmap);
                    image2 = uri;
                    return;
                }

                if (requestCode == PICK_IMAGE3) {
                    imageUpload3.setImageBitmap(bitmap);
                    image3 = uri;
                    return;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(Uri uri, String id, Integer i) {
        StorageReference imageUpload = mStorageRef.child("images/" + id + "/" + i.toString() + ".jpg");

        imageUpload.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // #Todo: borrar elemento de firestore?
                        Toast.makeText(AddMarkActivity.this, "No se ha podido completar la subida de imágenes!",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

}