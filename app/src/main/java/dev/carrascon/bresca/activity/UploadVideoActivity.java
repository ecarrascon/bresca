package dev.carrascon.bresca.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dev.carrascon.bresca.R;
import dev.carrascon.bresca.model.Video;

public class UploadVideoActivity extends AppCompatActivity {

    private static final int VIDEO_PICK_REQUEST = 100;
    private static final int IMAGE_PICK_REQUEST = 101;

    private EditText edtTitle, edtDescription;
    private ImageView imgThumbnail;
    private Uri thumbnailUri;

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
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        imgThumbnail = findViewById(R.id.imgThumbnail);

        imgThumbnail.setOnClickListener(v -> selectThumbnail());


    }

    private void selectThumbnail() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_REQUEST);
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
        } else if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            thumbnailUri = data.getData();
            imgThumbnail.setImageURI(thumbnailUri);
        }
    }

    private void uploadVideo() {
        if (videoUri != null && thumbnailUri != null) {
            progressDialog.show();

            // We generate a unique timestamp for each video uploaded.
            // This is done to prevent overwriting videos in Firebase Storage in case two videos have the same name.
            // Generamos una marca de tiempo única para cada video subido.
            // Esto se hace para evitar sobrescribir videos en Firebase Storage en caso de que dos videos tengan el mismo nombre.
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            StorageReference videoRef = storageReference.child("video_" + timeStamp);

            videoRef.putFile(videoUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return videoRef.getDownloadUrl();
            }).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                String videoDownloadUrl = task.getResult().toString();

                StorageReference thumbnailRef = storageReference.child("thumbnail_" + timeStamp);
                return thumbnailRef.putFile(thumbnailUri).continueWithTask(thumbnailTask -> {
                    if (!thumbnailTask.isSuccessful()) {
                        throw thumbnailTask.getException();
                    }
                    return thumbnailRef.getDownloadUrl();
                }).continueWith(thumbnailTask -> {
                    if (!thumbnailTask.isSuccessful()) {
                        throw thumbnailTask.getException();
                    }

                    String thumbnailDownloadUrl = thumbnailTask.getResult().toString();

                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users")
                            .child(firebaseAuth.getCurrentUser().getUid());

                    DatabaseReference newVideoRef = databaseReference.push();
                    String videoId = newVideoRef.getKey();

                    Video video = new Video(firebaseAuth.getCurrentUser().getUid(), videoDownloadUrl, timeStamp);
                    video.setVideoId(videoId);
                    video.setTitle(edtTitle.getText().toString().trim());
                    video.setDescription(edtDescription.getText().toString().trim());
                    video.setThumbnailUrl(thumbnailDownloadUrl);

                    // Add the videoId to the current user's videoIds list
                    userRef.child("videoIds").push().setValue(videoId);

                    return newVideoRef.setValue(video);
                });
            }).addOnCompleteListener(task -> {
                progressDialog.dismiss();

                if (task.isSuccessful()) {
                    Toast.makeText(UploadVideoActivity.this, "Video uploaded successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UploadVideoActivity.this, "Failed to upload video", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Please select a video and a thumbnail to upload", Toast.LENGTH_SHORT).show();
        }
    }
}
