package com.wolfgoes.popularmovies.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.wolfgoes.popularmovies.R;
import com.wolfgoes.popularmovies.data.MoviesContract;
import com.wolfgoes.popularmovies.model.Movie;
import com.wolfgoes.popularmovies.utils.Utility;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

public class DetailFragment extends Fragment {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;

    private static final String MOVIE_SHARE_HASHTAG = " #PopularMovies";
    private String mMovieStr;
    private FloatingActionButton mFavoriteButton;
    private Movie mMovie;
    private boolean mIsFavorite;
    private View mBackdropGradient;
    private AppBarLayout mAppBar;

    private MenuItem mMenuFavorite;

    private boolean mIsCollapsed = true;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        mAppBar = (AppBarLayout) rootView.findViewById(R.id.app_bar);

        mAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (!mIsCollapsed && mCollapsingToolbarLayout.getScrimVisibleHeightTrigger() - mAppBar.getMeasuredHeight() >= verticalOffset) {
                    mIsCollapsed = true;
                    if (mMenuFavorite != null) {
                        getActivity().invalidateOptionsMenu();
                    }
                } else if (mIsCollapsed && mCollapsingToolbarLayout.getScrimVisibleHeightTrigger() - mAppBar.getMeasuredHeight() < verticalOffset) {
                    mIsCollapsed = false;
                    if (mMenuFavorite != null) {
                        getActivity().invalidateOptionsMenu();
                    }
                }
            }
        });

        mBackdropGradient = rootView.findViewById(R.id.backdrop_gradient);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);

        mFavoriteButton = (FloatingActionButton) rootView.findViewById(R.id.favorite);

        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFavoriteAction();
            }
        });

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra("movie")) {
                mMovie = intent.getParcelableExtra("movie");
                if (mMovie != null) {
                    //Get all values
                    String title = mMovie.getTitle();
                    String releaseDate = mMovie.getReleaseDate();
                    String poster = mMovie.getPosterPath();
                    final String backdrop = mMovie.getBackdropPath();
                    double vote = mMovie.getVoteAverage();
                    String synopsis = mMovie.getSynopsis();

                    //Get all view
                    TextView releaseView = (TextView) rootView.findViewById(R.id.year);
                    final ImageView backdropView = (ImageView) rootView.findViewById(R.id.backdrop);
                    ImageView posterView = (ImageView) rootView.findViewById(R.id.poster);
                    TextView voteView = (TextView) rootView.findViewById(R.id.rate);
                    TextView overviewView = (TextView) rootView.findViewById(R.id.synopsis);

                    //Set values to the views
                    mMovieStr = title;

                    mCollapsingToolbarLayout.setTitle(title);
                    releaseView.setText(releaseDate);
                    Glide.with(getContext())
                            .load(Utility.getPosterUrlForMovie(poster, null))
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(posterView);

                    Glide.with(getContext())
                            .load(Utility.getPosterUrlForMovie(backdrop, "w1280"))
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    mBackdropGradient.setVisibility(View.VISIBLE);
                                    backdropView.setImageBitmap(resource);
                                    dynamicToolbarColor(resource);

                                }
                            });

                    voteView.setText(Double.toString(vote) + "/10");
                    overviewView.setText(synopsis);

                }
            }
        }

        setFavorite();

        mFavoriteButton.setImageResource(mIsFavorite ? R.drawable.ic_favorite_true : R.drawable.ic_favorite_false);

        return rootView;
    }

    private void setFavoriteAction() {
        if (mMovie != null) {
            if (mIsFavorite) {
                int rows = getContext().getContentResolver().delete(MoviesContract.MovieEntry.buildMovieWithIdUri(mMovie.getId()), null, null);

                if (rows == 1) {
                    mIsFavorite = false;
                    mFavoriteButton.setImageResource(R.drawable.ic_favorite_false);
                    getActivity().invalidateOptionsMenu();
                }
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MoviesContract.MovieEntry._ID, mMovie.getId());
                contentValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, mMovie.getTitle());
                contentValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_URL, mMovie.getPosterPath());
                contentValues.put(MoviesContract.MovieEntry.COLUMN_BACKDROP_URL, mMovie.getBackdropPath());
                contentValues.put(MoviesContract.MovieEntry.COLUMN_SYNOPSIS, mMovie.getSynopsis());
                contentValues.put(MoviesContract.MovieEntry.COLUMN_RATING, mMovie.getVoteAverage());
                contentValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE, mMovie.getReleaseDate());

                Uri uri = getContext().getContentResolver().insert(MoviesContract.MovieEntry.CONTENT_URI, contentValues);

                if (uri != null) {
                    mIsFavorite = true;
                    mFavoriteButton.setImageResource(R.drawable.ic_favorite_true);
                    getActivity().invalidateOptionsMenu();
                }
            }
        }
    }

    private void dynamicToolbarColor(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                float[] hsv = new float[3];
                int color = palette.getVibrantColor(palette.getMutedColor(R.attr.colorPrimary));
                Color.colorToHSV(color, hsv);
                hsv[2] *= 0.8f; // value component
                int darkColor = Color.HSVToColor(hsv);

                mCollapsingToolbarLayout.setContentScrimColor(color);
                mCollapsingToolbarLayout.setStatusBarScrimColor(darkColor);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        mMenuFavorite = menu.findItem(R.id.action_favorite);
        mMenuFavorite.setIcon(mIsFavorite ? R.drawable.ic_favorite_true_white : R.drawable.ic_favorite_false_white);
        mMenuFavorite.setVisible(mIsCollapsed);
        mMenuFavorite.setEnabled(mIsCollapsed);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                actionShare();
                return true;
            case R.id.action_favorite:
                setFavoriteAction();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setFavorite() {
        if (mMovie != null) {
            Cursor movie = getContext().getContentResolver().query(
                    MoviesContract.MovieEntry.buildMovieWithIdUri(mMovie.getId()),
                    null,
                    null,
                    null,
                    null
            );

            if (movie != null) {
                getActivity().invalidateOptionsMenu();
                mIsFavorite = movie.moveToFirst();
                movie.close();
            }
        }
    }

    private void actionShare() {
        Intent textShareIntent = new Intent(Intent.ACTION_SEND);
        textShareIntent.setType("text/plain");
        textShareIntent.putExtra(Intent.EXTRA_TEXT, mMovieStr + MOVIE_SHARE_HASHTAG);
        if (textShareIntent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivity(Intent.createChooser(textShareIntent, "Share"));
    }
}
