package dev.carrascon.bresca.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dev.carrascon.bresca.R;
import dev.carrascon.bresca.model.User;

public class UserProfileActivity extends AppCompatActivity {
    private ImageView imgProfile;
    private TextView txtName, txtFollowers, txtFollowing;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference followersRef, followingRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_user_profile);

        imgProfile = findViewById(R.id.imgProfile);
        txtName = findViewById(R.id.txtName);
        txtFollowers = findViewById(R.id.followersCount);
        txtFollowing = findViewById(R.id.followingCount);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        String userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                userId = user.getUid();
            }
        }

        if(userId != null) {
            followersRef = database.getReference().child("Followers").child(userId);
            followingRef = database.getReference().child("Following").child(userId);

            database.getReference().child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User userApp = snapshot.getValue(User.class);
                    if (userApp != null) {
                        txtName.setText(userApp.getName());
                        if(!isDestroyed()) {
                            Glide.with(UserProfileActivity.this)
                                    .load(userApp.getProfile())
                                    .into(imgProfile);
                        }


                        updateFollowersCount();
                        updateFollowingCount();

                        txtFollowers.setOnClickListener(v -> {
                            Intent followersIntent = new Intent(UserProfileActivity.this, UserListActivity.class);
                            followersIntent.putExtra("userId", userApp.getUserId());
                            followersIntent.putExtra("listType", "Followers");
                            startActivity(followersIntent);
                        });

                        txtFollowing.setOnClickListener(v -> {
                            Intent followingIntent = new Intent(UserProfileActivity.this, UserListActivity.class);
                            followingIntent.putExtra("userId", userApp.getUserId());
                            followingIntent.putExtra("listType", "Following");
                            startActivity(followingIntent);
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void updateFollowersCount() {
        followersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long followersCount = snapshot.getChildrenCount();
                String followersText = getResources().getString(R.string.followers_numbers, followersCount);
                txtFollowers.setText(followersText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, R.string.failed_load_followers, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFollowingCount() {
        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long followingCount = snapshot.getChildrenCount();
                String followingText = getResources().getString(R.string.following_numbers, followingCount);
                txtFollowing.setText(followingText);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, R.string.failed_load_following, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
