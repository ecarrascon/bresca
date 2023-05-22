package dev.carrascon.bresca.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dev.carrascon.bresca.R;
import dev.carrascon.bresca.model.ScheduledVideo;
import dev.carrascon.bresca.model.Video;

public class UserCalendarActivity extends AppCompatActivity {

    private String currentUserId;
    private LinearLayout videosLayout;
    private DatabaseReference scheduledVideosRef;
    private DatabaseReference videosRef;
    private Button dateSelectButton;
    private RecyclerView videosRecyclerView;
    private VideosAdapter videosAdapter;
    private List<Video> videoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_calendar);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        scheduledVideosRef = FirebaseDatabase.getInstance().getReference("ScheduledVideos");
        videosRef = FirebaseDatabase.getInstance().getReference("Videos");

        dateSelectButton = findViewById(R.id.date_select_button);
        dateSelectButton.setOnClickListener(v -> openCalendar());

        videosRecyclerView = findViewById(R.id.videos_recycler_view);

        videosAdapter = new VideosAdapter(videoList);
        videosRecyclerView.setAdapter(videosAdapter);
        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    private void openCalendar() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(UserCalendarActivity.this, (view, year1, month1, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1, month1, dayOfMonth);
            loadScheduledVideosForDate(selectedDate.getTimeInMillis());
        }, year, month, day);

        datePickerDialog.show();
    }

    private void loadScheduledVideosForDate(long date) {
        Calendar selectedDateCalendar = Calendar.getInstance();
        selectedDateCalendar.setTimeInMillis(date);

        videoList.clear();
        videosAdapter.notifyDataSetChanged();
        scheduledVideosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ScheduledVideo scheduledVideo = snapshot.getValue(ScheduledVideo.class);

                    if (scheduledVideo.getUserId().equals(currentUserId)) {
                        Calendar scheduledVideoCalendar = Calendar.getInstance();
                        scheduledVideoCalendar.setTimeInMillis(scheduledVideo.getScheduledDate());

                        if (selectedDateCalendar.get(Calendar.YEAR) == scheduledVideoCalendar.get(Calendar.YEAR) &&
                                selectedDateCalendar.get(Calendar.MONTH) == scheduledVideoCalendar.get(Calendar.MONTH) &&
                                selectedDateCalendar.get(Calendar.DAY_OF_MONTH) == scheduledVideoCalendar.get(Calendar.DAY_OF_MONTH)) {
                            loadVideoDetails(scheduledVideo.getVideoId());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle database error here
            }
        });
    }


    private void loadVideoDetails(String videoId) {
        videosRef.child(videoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Video video = dataSnapshot.getValue(Video.class);
                videoList.add(video);
                videosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle database error here
            }
        });
    }

    private class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {

        private List<Video> videoList;

        public VideosAdapter(List<Video> videoList) {
            this.videoList = videoList;
        }

        @Override
        public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
            return new VideoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(VideoViewHolder holder, int position) {
            Video video = videoList.get(position);
            holder.titleTextView.setText(video.getTitle());

            Glide.with(UserCalendarActivity.this)
                    .load(video.getThumbnailUrl())
                    .into(holder.thumbnailImageView);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(video.getVideoUrl()));
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return videoList.size();
        }

        public class VideoViewHolder extends RecyclerView.ViewHolder {

            ImageView thumbnailImageView;
            TextView titleTextView;

            public VideoViewHolder(View itemView) {
                super(itemView);

                thumbnailImageView = itemView.findViewById(R.id.video_thumbnail);
                titleTextView = itemView.findViewById(R.id.video_title);
            }
        }
    }
}
