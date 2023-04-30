package dev.carrascon.bresca;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.widget.Button;
import android.app.DatePickerDialog;
import android.widget.Toast;

import java.util.Calendar;


public class VideoFragment extends Fragment {

    private static final String ARG_VIDEO_URL = "videoUrl";
    private static final String ARG_VIDEO_ID = "videoId";

    private String videoUrl;
    private String videoId;


    private com.google.android.exoplayer2.ui.PlayerView exoPlayerView;
    private SimpleExoPlayer exoPlayer;
    private Button scheduleButton;


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

        exoPlayerView = view.findViewById(R.id.exoPlayerView);
        initializePlayer();
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
                    .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Video scheduled!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to schedule video", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializePlayer() {
        exoPlayer = new SimpleExoPlayer.Builder(requireContext()).build();
        exoPlayerView.setPlayer(exoPlayer);
        exoPlayerView.setUseController(false);

        // Set the video scaling mode
        exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(requireContext(), Util.getUserAgent(requireContext(), "Bresca"));
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)));

        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);

        exoPlayerView.setOnClickListener(v -> exoPlayer.setPlayWhenReady(!exoPlayer.isPlaying()));
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