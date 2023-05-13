package dev.carrascon.bresca.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;

import dev.carrascon.bresca.R;

public class PrincipalActivity extends AppCompatActivity {

    private Button btnGoToUpload;
    private Button btnGoToWatch;
    private Button btnLogout;
    private FirebaseAuth auth;
    private GoogleSignInClient managerGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        managerGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoToUpload = findViewById(R.id.btnGoToUpload);
        btnGoToWatch = findViewById(R.id.btnGoToWatch);
        btnLogout = findViewById(R.id.btnLogout);

        btnGoToWatch.setOnClickListener(v -> {
            Intent watchIntent = new Intent(PrincipalActivity.this, VideoFeedActivity.class);
            startActivity(watchIntent);
        });

        btnGoToUpload.setOnClickListener(v -> {
            Intent uploadIntent = new Intent(PrincipalActivity.this, UploadVideoActivity.class);
            startActivity(uploadIntent);
        });

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            managerGoogleSignInClient.signOut();

            Intent logoutIntent = new Intent(PrincipalActivity.this, MainActivity.class);
            startActivity(logoutIntent);
            finish();
        });
    }
}
