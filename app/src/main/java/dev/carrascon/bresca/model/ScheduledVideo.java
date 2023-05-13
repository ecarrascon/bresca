package dev.carrascon.bresca.model;

public class ScheduledVideo {
    private String userId;
    private String videoId;
    private long scheduledDate;

    public ScheduledVideo() {
    }

    public ScheduledVideo(String userId, String videoId, long scheduledDate) {
        this.userId = userId;
        this.videoId = videoId;
        this.scheduledDate = scheduledDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public long getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(long scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
}
