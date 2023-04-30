package dev.carrascon.bresca;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.viewpager2.widget.ViewPager2;

public class VideoFeedActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private VideoAdapter videoAdapter;
    private List<Video> videoList;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_feed);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        viewPager = findViewById(R.id.viewPager);

        videoList = new ArrayList<>();
        videoAdapter = new VideoAdapter(this, videoList);
        viewPager.setAdapter(videoAdapter);
        databaseReference = FirebaseDatabase.getInstance().getReference("Videos");

        fetchVideos();
    }

    private void fetchVideos() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                videoList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Video video = snapshot.getValue(Video.class);
                    videoList.add(video);
                }
                videoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }
}
