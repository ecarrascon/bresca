package dev.carrascon.bresca.adapter;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import dev.carrascon.bresca.fragment.VideoFragment;
import dev.carrascon.bresca.model.Video;

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
        return VideoFragment.newInstance(videoList.get(position).getVideoUrl(), videoList.get(position).getVideoId());
    }


    @Override
    public int getItemCount() {
        return videoList.size();
    }
}