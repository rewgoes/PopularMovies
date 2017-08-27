package com.wolfgoes.popularmovies.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wolfgoes.popularmovies.R;
import com.wolfgoes.popularmovies.listener.OnLoadMoreListener;
import com.wolfgoes.popularmovies.model.Movie;
import com.wolfgoes.popularmovies.utils.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.wolfgoes.popularmovies.utils.Utility.FILE_DIRECTORY;

// Load more on scroll is based on: http://www.devexchanges.info/2017/02/android-recyclerview-dynamically-load.html
class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_MOVIE = 0;
    public static final int VIEW_TYPE_LOADING = 1;

    private OnLoadMoreListener mOnLoadMoreListener;

    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean mFavorite;

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

    public void setFavorite(boolean favorite) {
        mFavorite = favorite;
    }

    private static class MovieViewHolder extends RecyclerView.ViewHolder {

        ImageView moviePoster;

        MovieViewHolder(View v) {
            super(v);
            moviePoster = (ImageView) v.findViewById(R.id.poster);
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;

        private LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.loading);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mMovies.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_MOVIE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MOVIE) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.movie_item, parent, false);
            return new MovieViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.loading_item, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MovieViewHolder) {
            MovieViewHolder movieViewHolder = (MovieViewHolder) holder;
            final int adapterPosition = movieViewHolder.getAdapterPosition();
            Movie movie = mMovies.get(adapterPosition);

            Uri posterPath;
            if (mFavorite) {
                posterPath = Uri.fromFile(new File(Environment.getExternalStorageDirectory() +
                        String.format(FILE_DIRECTORY, mContext.getApplicationContext().getPackageName(), movie.getId()) + "/poster.jpg"));
            } else {
                posterPath = Uri.parse(Utility.getPosterUrlForMovie(movie.getPosterPath(), null));
            }

            Glide.with(mContext)
                    .load(posterPath)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(movieViewHolder.moviePoster);

            movieViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DetailActivity.class)
                            .putExtra("movie", mMovies.get(adapterPosition));
                    mContext.startActivity(intent);
                }
            });
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return mMovies == null ? 0 : mMovies.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener, RecyclerView recyclerView) {
        this.mOnLoadMoreListener = onLoadMoreListener;
        recyclerView.clearOnScrollListeners();

        final GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();

        if (mOnLoadMoreListener != null) {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    totalItemCount = gridLayoutManager.getItemCount();
                    lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();
                    if (!isLoading && totalItemCount > 0 && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (mOnLoadMoreListener != null) {
                            mOnLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            });
        }
    }

    public void setLoaded() {
        isLoading = false;
    }

}
