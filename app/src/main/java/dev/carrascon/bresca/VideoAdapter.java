package dev.carrascon.bresca;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class VideoAdapter extends FragmentStateAdapter {

    private Context context;
    private List<Video> videoList;

    public VideoAdapter(FragmentActivity fragmentActivity, List<Video> videoList) {
        super(fragmentActivity);
        this.context = fragmentActivity;
        this.videoList = videoList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Video video = videoList.get(position);
        return VideoFragment.newInstance(video.getVideoUrl());
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }
}