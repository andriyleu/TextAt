package com.andriy.textat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {


    // UI
    private TextView email;
    private TextView usernick;
    private Toolbar toolbar;
    private Button saveButton;

    // Firebase
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Avatar
    private ImageView avatar;
    private Uri imageLink;


    // Logic
    public static final int PICK_AVATAR = 1;
    private boolean newAvatar = false;
    private String usernickname;
    private boolean userNickExists = false;
    private boolean isRegister = false;


    private void bindElements() {
        email = findViewById(R.id.userEmail);
        usernick = findViewById(R.id.userNick);
        avatar = findViewById(R.id.userAvatar);
        toolbar = findViewById(R.id.settingsToolbar);
        saveButton = findViewById(R.id.saveInfo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Bind UI elements
        bindElements();

        // Get user info from MainActivity
        Intent i = getIntent();
        user = i.getExtras().getParcelable("user");
        usernickname = i.getExtras().getString("usernick");
        if (usernickname == null) {
            isRegister = true;
        }

        // Firebase
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // set ui stuff
        usernick.setText(usernickname);
        email.setText(user.getEmail());

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePicker();
            }
        });

        if (!isRegister) {
            setImage();
        }

        // toolbar

        setSupportActionBar(toolbar);

        if (usernickname != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } else {
            getSupportActionBar().setTitle("Actualiza tu información");
        }

        // save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInfo();
            }
        });

    }

    /////////// IMAGES
    private void setImage() {
        String name = ((TextAt) getApplication()).getUsernick();
        final StorageReference ref = FirebaseStorage.getInstance().getReference().child("users/".concat(name).concat(".jpg"));

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                setAvatarImage(uri);
            }
        });

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePicker();
            }
        });
    }

    private void setAvatarImage(Uri uri) {
        Glide.with(getApplicationContext()).load(uri.toString()).apply(RequestOptions.circleCropTransform()).into(avatar);
    }

    public void imagePicker() {
        String[] mimeTypes = {"image/jpeg"};

        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        getIntent.setType("image/*");


        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});


        startActivityForResult(chooserIntent, PICK_AVATAR);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && data != null) {
            Uri uri = data.getData();

            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                if (requestCode == PICK_AVATAR) {

                    imageLink = uri;
                    newAvatar = true;
                    setAvatarImage(uri);

                    return;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(Uri uri) {
        StorageReference imageUpload = mStorageRef.child("users/".concat(usernick.getText().toString()).concat(".jpg"));

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
                        Toast.makeText(SettingsActivity.this, "No se ha podido completar la subida de imágenes!",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /////////// Save logic

    private void saveInfo() {

        String editNick = usernick.getText().toString();

        if (isRegister) {
            if (editNick.length() > 0) {
                checkUsernameExists(usernick.getText().toString());
            } else {
                usernick.setError("Debes elegir un nick!");
            }
        } else {
            if (!usernickname.equals(editNick)) {
                checkUsernameExists(editNick);
            } else {
                if (newAvatar) {
                    uploadImage(imageLink);
                }
            }
        }
    }

    private void checkUsernameExists(String usernick) {
        db.collection("usernames").document(usernick).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot d = task.getResult();
                    continueRegister(d.exists() ? true : false);
                }
            }
        });
    }


    private void continueRegister(Boolean exists) {
        if (exists) {
            usernick.setError("El usuario ya existe.");
        } else {

            // borrar nickname viejo
            if (!isRegister) {
                db.collection("usernames").document(usernickname).delete();
            }

            // añadir nickname nuevo
            Map<String, String> u = new HashMap<>();
            u.put("email", user.getEmail());
            u.put("uid", user.getUid());

            db.collection("usernames").document(usernick.getText().toString()).set(u).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    Map<String, String> uid = new HashMap<>();
                    uid.put("nick", usernick.getText().toString());

                    if (newAvatar) {
                        uploadImage(imageLink);
                    }

                    // modificar uid
                    db.collection("uids").document(user.getUid()).set(uid).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (isRegister) {
                                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                                intent.putExtra("user", user);
                                startActivity(intent);
                            }
                            finish();
                        }
                    });
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
