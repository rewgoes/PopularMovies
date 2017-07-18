package com.wolfgoes.popularmovies.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wolfgoes.popularmovies.R;
import com.wolfgoes.popularmovies.model.Video;
import com.wolfgoes.popularmovies.utils.Utility;

import java.util.ArrayList;
import java.util.List;

class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    void setVideos(List<Video> videos) {
        if (videos == null) {
            videos = new ArrayList<>();
        }
        mVideos = videos;
    }

    private List<Video> mVideos;
    private Context mContext;

    VideoAdapter(Context context1, List<Video> videos) {
        mVideos = videos;
        mContext = context1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView videoImage;
        TextView videoTitle;

        ViewHolder(View v) {
            super(v);
            videoImage = (ImageView) v.findViewById(R.id.video_image);
            videoTitle = (TextView) v.findViewById(R.id.video_title);
        }
    }

    @Override
    public VideoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.video_item, parent, false));
    }

    @Override
    public void onBindViewHolder(VideoAdapter.ViewHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();

        holder.videoTitle.setText(mVideos.get(position).getName());

        Glide.with(mContext)
                .load(Utility.getVideoThumbnail(mVideos.get(adapterPosition).getKey()))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.videoImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openYoutubeVideo(mVideos.get(adapterPosition).getKey());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    private void openYoutubeVideo(String key) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + key));
        try {
            mContext.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            try {
                mContext.startActivity(webIntent);
            } catch (ActivityNotFoundException ex2) {
                Toast.makeText(mContext, "Please, install Youtube or any Browser", Toast.LENGTH_LONG).show();
            }
        }
    }
}
