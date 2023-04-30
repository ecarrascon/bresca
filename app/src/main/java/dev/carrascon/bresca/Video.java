package dev.carrascon.bresca;

public class Video {
    private String userId;
    private String videoUrl;
    private String timestamp;

    private String videoId;


    public Video() {
    }

    public Video(String userId, String videoUrl, String timestamp) {
        this.userId = userId;
        this.videoUrl = videoUrl;
        this.timestamp = timestamp;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
