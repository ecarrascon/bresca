package dev.carrascon.bresca;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadVideoActivity extends AppCompatActivity {

    private static final int VIDEO_PICK_REQUEST = 100;
    private Uri videoUri;

    private Button btnSelectVideo;
    private Button btnUploadVideo;

    private ProgressDialog progressDialog;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        btnSelectVideo = findViewById(R.id.btnSelectVideo);
        btnUploadVideo = findViewById(R.id.btnUploadVideo);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Video");
        progressDialog.setMessage("Please wait while we upload your video...");

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Videos");
        databaseReference = FirebaseDatabase.getInstance().getReference("Videos");

        btnSelectVideo.setOnClickListener(v -> selectVideo());
        btnUploadVideo.setOnClickListener(v -> uploadVideo());
    }

    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, VIDEO_PICK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIDEO_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            videoUri = data.getData();
        }
    }

    private void uploadVideo() {
        if (videoUri != null) {
            progressDialog.show();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            StorageReference videoRef = storageReference.child("video_" + timeStamp);

            videoRef.putFile(videoUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return videoRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

                    DatabaseReference newVideoRef = databaseReference.push();
                    newVideoRef.setValue(new Video(firebaseAuth.getCurrentUser().getUid(), downloadUri.toString(), timeStamp));

                    progressDialog.dismiss();
                    Toast.makeText(UploadVideoActivity.this, "Video uploaded successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(UploadVideoActivity.this, "Failed to upload video", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Please select a video to upload", Toast.LENGTH_SHORT).show();
        }
    }
}
