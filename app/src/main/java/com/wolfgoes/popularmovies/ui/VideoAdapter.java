package com.wolfgoes.popularmovies.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wolfgoes.popularmovies.R;
import com.wolfgoes.popularmovies.model.Video;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.ArrayList;
import java.util.List;

class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private Parser mParser = Parser.builder().build();
    private HtmlRenderer mRenderer = HtmlRenderer.builder().build();

    void setVideos(List<Video> videos) {
        if (videos == null) {
            videos = new ArrayList<>();
        }
        mVideos = videos;
    }

    private List<Video> mVideos;
    private Context context;

    VideoAdapter(Context context1, List<Video> videos) {
        mVideos = videos;
        context = context1;
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
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.video_item, parent, false));
    }

    @Override
    public void onBindViewHolder(VideoAdapter.ViewHolder holder, int position) {
        holder.videoTitle.setText(mVideos.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }
}
