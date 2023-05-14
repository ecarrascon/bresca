package dev.carrascon.bresca.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

import dev.carrascon.bresca.R;
import dev.carrascon.bresca.model.User;

public class MainActivity extends AppCompatActivity {

    ImageButton btnSignIn;
    FirebaseAuth auth;
    FirebaseDatabase database;
    GoogleSignInClient managerGoogleSignInClient;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Check if the user is already signed in.
        // This step is necessary to prevent the user from having to sign in every time the app is opened.
        // Comprueba si el usuario ya ha iniciado sesión.
        // Este paso es necesario para evitar que el usuario tenga que iniciar sesión cada vez que abre la aplicación.
        if (currentUser != null) {
            Intent intent = new Intent(MainActivity.this, PrincipalActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_main);

        btnSignIn = findViewById(R.id.btnGoogleSignIn);

        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Creating account");
        progressDialog.setMessage("We are creating your account!");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        managerGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnSignIn.setOnClickListener(v -> {
            signIn();
        });
    }

    int RC_SIGN_IN = 40;

    private void signIn() {
        Intent intent = managerGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                firebaseAuth(account.getIdToken());
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }

        }

    }

    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();

                        User userApp = new User();
                        userApp.setUserId(user.getUid());
                        userApp.setName(user.getDisplayName());
                        userApp.setProfile(Objects.requireNonNull(user.getPhotoUrl()).toString());

                        database.getReference().child("Users").child(user.getUid()).setValue(userApp);

                        Intent intent = new Intent(MainActivity.this, PrincipalActivity.class);
                        startActivity(intent);
                    } else {


                        Toast.makeText(MainActivity.this, "Sorry, authentication failed", Toast.LENGTH_SHORT).show();

                    }

                });

    }
}