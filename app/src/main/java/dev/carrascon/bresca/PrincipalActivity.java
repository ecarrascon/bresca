package dev.carrascon.bresca;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class PrincipalActivity extends AppCompatActivity {

    private Button btnGoToUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        btnGoToUpload = findViewById(R.id.btnGoToUpload);

        btnGoToUpload.setOnClickListener(v -> {
            Intent uploadIntent = new Intent(PrincipalActivity.this, UploadVideoActivity.class);
            startActivity(uploadIntent);
        });
    }
}
