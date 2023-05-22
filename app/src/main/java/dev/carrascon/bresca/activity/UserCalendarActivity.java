package dev.carrascon.bresca.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import dev.carrascon.bresca.R;
import dev.carrascon.bresca.model.ScheduledVideo;
import dev.carrascon.bresca.model.Video;

public class UserCalendarActivity extends AppCompatActivity {

    private LinearLayout videosLayout;
    private DatabaseReference scheduledVideosRef;
    private DatabaseReference videosRef;
    private Button dateSelectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_calendar);

        videosLayout = findViewById(R.id.videos_layout);
        scheduledVideosRef = FirebaseDatabase.getInstance().getReference("ScheduledVideos");
        videosRef = FirebaseDatabase.getInstance().getReference("Videos");

        dateSelectButton = findViewById(R.id.date_select_button);
        dateSelectButton.setOnClickListener(v -> openCalendar());
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

        scheduledVideosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ScheduledVideo scheduledVideo = snapshot.getValue(ScheduledVideo.class);

                    Calendar scheduledVideoCalendar = Calendar.getInstance();
                    scheduledVideoCalendar.setTimeInMillis(scheduledVideo.getScheduledDate());

                    if (selectedDateCalendar.get(Calendar.YEAR) == scheduledVideoCalendar.get(Calendar.YEAR) &&
                            selectedDateCalendar.get(Calendar.MONTH) == scheduledVideoCalendar.get(Calendar.MONTH) &&
                            selectedDateCalendar.get(Calendar.DAY_OF_MONTH) == scheduledVideoCalendar.get(Calendar.DAY_OF_MONTH)) {
                        loadVideoDetails(scheduledVideo.getVideoId());
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

                Button thumbnailButton = new Button(UserCalendarActivity.this);
                thumbnailButton.setText(video.getTitle());
                thumbnailButton.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(video.getVideoUrl()));
                    startActivity(intent);
                });

                videosLayout.addView(thumbnailButton);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle database error here
            }
        });
    }
}
