package com.andriy.textat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    final int RC_SIGN_IN = 123;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) { // Si el usuario está logueado ya
            switchToHome(auth.getCurrentUser());
            return;
        }

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Lanzar intent con los diferentes proveedores de autentificación
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme((R.style.LoginTheme))
                        .setLogo(R.drawable.textat)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                db.collection("uids").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot d = task.getResult();
                            if (d.exists()) {
                                // Usuario logueado y con nickname, cambiar a "Home"
                                switchToHome(user);
                            }
                            else {
                                switchUserSettings(user);
                            }
                        }
                    }
                });

            } else {
                // #Todo: mostrar mensaje de error
            }
        }

    }

    private void switchToHome(FirebaseUser user) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }

    private void switchUserSettings(FirebaseUser user) {
        Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }

}
