package dev.carrascon.bresca;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class PrincipalActivity extends AppCompatActivity {

    private Button btnGoToUpload;
    private Button btnGoToWatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        btnGoToUpload = findViewById(R.id.btnGoToUpload);
        btnGoToWatch = findViewById(R.id.btnGoToWatch);

        btnGoToWatch.setOnClickListener(v -> {
            Intent watchIntent = new Intent(PrincipalActivity.this, VideoFeedActivity.class);
            startActivity(watchIntent);
        });

        btnGoToUpload.setOnClickListener(v -> {
            Intent uploadIntent = new Intent(PrincipalActivity.this, UploadVideoActivity.class);
            startActivity(uploadIntent);
        });
    }
}
