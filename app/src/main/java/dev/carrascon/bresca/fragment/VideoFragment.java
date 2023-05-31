package dev.carrascon.bresca.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.Button;
import android.app.DatePickerDialog;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import dev.carrascon.bresca.R;
import dev.carrascon.bresca.activity.UserProfileActivity;
import dev.carrascon.bresca.model.ScheduledVideo;
import dev.carrascon.bresca.model.User;
import dev.carrascon.bresca.model.Video;


public class VideoFragment extends Fragment {

    private static final String ARG_VIDEO_URL = "videoUrl";
    private static final String ARG_VIDEO_ID = "videoId";

    private String videoUrl;
    private String videoId;
    private TextView tvScheduledUsers;

    private TextView tvTitle;
    private TextView tvUploader;

    private Button btnShowDescription;


    private com.google.android.exoplayer2.ui.PlayerView exoPlayerView;
    private ExoPlayer exoPlayer;
    private Button scheduleButton;
    private Button followButton;
    private Video video;


    public VideoFragment() {
    }

    public static VideoFragment newInstance(String videoUrl, String videoId) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_URL, videoUrl);
        args.putString(ARG_VIDEO_ID, videoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videoUrl = getArguments().getString(ARG_VIDEO_URL);
            videoId = getArguments().getString(ARG_VIDEO_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.item_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scheduleButton = view.findViewById(R.id.scheduleButton);
        scheduleButton.setOnClickListener(v -> openCalendar());

        tvTitle = view.findViewById(R.id.tvTitle);
        tvUploader = view.findViewById(R.id.tvUploader);

        btnShowDescription = view.findViewById(R.id.btnShowDescription);
        btnShowDescription.setOnClickListener(v -> showDescriptionDialog());

        tvScheduledUsers = view.findViewById(R.id.tv_scheduled_users);

        followButton = view.findViewById(R.id.followButton);
        followButton.setOnClickListener(v -> followUser());

        exoPlayerView = view.findViewById(R.id.exoPlayerView);
        initializePlayer();
        updateScheduledUsersCount();
        fetchVideoData();


    }

    private void fetchVideoData() {
        DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference("Videos").child(videoId);
        videoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                video = dataSnapshot.getValue(Video.class);
                tvTitle.setText(video.getTitle());
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(video.getUserId());
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            tvUploader.setText(user.getName());
                            tvUploader.setPaintFlags(tvUploader.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            tvUploader.setOnClickListener(v -> openUserProfile(user.getUserId()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                updateFollowButton();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void showDescriptionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.video_recipe);

        final ScrollView scrollView = new ScrollView(requireContext());
        final TextView tvDescription = new TextView(requireContext());
        tvDescription.setPadding(16, 16, 16, 16);
        tvDescription.setText(video.getDescription());
        scrollView.addView(tvDescription);

        builder.setView(scrollView);

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void updateFollowButton() {
        if (video != null) {
            String videoUserId = video.getUserId();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && !currentUser.getUid().equals(videoUserId)) {
                DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Following").child(currentUser.getUid()).child(videoUserId);
                followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            followButton.setText("Unfollow");
                        } else {
                            followButton.setText("Follow");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            } else {
                followButton.setVisibility(View.GONE);
            }
        }
    }
    private void followUser() {
        if (video != null) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String currentUserId = currentUser.getUid();
                String videoUserId = video.getUserId();
                DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Followers").child(videoUserId).child(currentUserId);
                DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Following").child(currentUserId).child(videoUserId);
                if (followButton.getText().equals("Follow")) {
                    followersRef.setValue(true);
                    followingRef.setValue(true);
                    followButton.setText("Unfollow");
                } else {
                    followersRef.removeValue();
                    followingRef.removeValue();
                    followButton.setText("Follow");
                }
            }
        }
    }
    private void openUserProfile(String userId) {
        Intent intent = new Intent(requireContext(), UserProfileActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }
    private void openCalendar() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year1, month1, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1, month1, dayOfMonth);
            scheduleVideo(selectedDate.getTimeInMillis());
        }, year, month, day);

        datePickerDialog.show();
    }

    private void scheduleVideo(long scheduledDate) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ScheduledVideos");
            ScheduledVideo scheduledVideo = new ScheduledVideo(userId, videoId, scheduledDate);
            databaseReference.push().setValue(scheduledVideo)
                    .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), R.string.video_scheduled, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), R.string.failed_schedule, Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(requireContext(), "Usuario no registrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializePlayer() {
        exoPlayer = new SimpleExoPlayer.Builder(requireContext()).build();
        exoPlayerView.setPlayer(exoPlayer);
        exoPlayerView.setUseController(false);

        exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(requireContext(), Util.getUserAgent(requireContext(), "Bresca"));
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)));

        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);

        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);


        exoPlayerView.setOnClickListener(v -> exoPlayer.setPlayWhenReady(!exoPlayer.isPlaying()));
    }


    private void updateScheduledUsersCount() {
        DatabaseReference scheduledVideosRef = FirebaseDatabase.getInstance().getReference("ScheduledVideos");
        long startOfDay = getStartOfDayTimestamp();
        long endOfDay = getEndOfDayTimestamp();

        scheduledVideosRef.orderByChild("scheduledDate").startAt(startOfDay).endAt(endOfDay).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ScheduledVideo scheduledVideo = snapshot.getValue(ScheduledVideo.class);
                    if (scheduledVideo != null && scheduledVideo.getVideoId().equals(videoId)) {
                        count++;
                    }
                }
                tvScheduledUsers.setText(count + getString(R.string.cook_today));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private long getStartOfDayTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfDayTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
