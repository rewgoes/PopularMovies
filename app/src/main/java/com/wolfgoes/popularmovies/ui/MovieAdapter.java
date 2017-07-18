package com.wolfgoes.popularmovies.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wolfgoes.popularmovies.R;
import com.wolfgoes.popularmovies.model.Movie;
import com.wolfgoes.popularmovies.utils.Utility;

import java.util.ArrayList;
import java.util.List;

class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    void setMovies(List<Movie> movies) {
        if (movies == null) {
            movies = new ArrayList<>();
        }
        mMovies = movies;
    }

    private List<Movie> mMovies;
    private Context mContext;

    MovieAdapter(Context context, List<Movie> movies) {
        mMovies = movies;
        mContext = context;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView moviePoster;

        ViewHolder(View v) {
            super(v);
            moviePoster = (ImageView) v.findViewById(R.id.poster);
        }
    }

    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.movie_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MovieAdapter.ViewHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();

        Glide.with(mContext)
                .load(Utility.getPosterUrlForMovie(mMovies.get(adapterPosition).getPosterPath(), null))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.moviePoster);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DetailActivity.class)
                        .putExtra("movie", mMovies.get(adapterPosition));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }
}
