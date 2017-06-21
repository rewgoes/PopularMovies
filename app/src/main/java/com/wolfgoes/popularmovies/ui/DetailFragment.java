package com.wolfgoes.popularmovies.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wolfgoes.popularmovies.R;
import com.wolfgoes.popularmovies.data.MoviesContract;
import com.wolfgoes.popularmovies.model.Movie;
import com.wolfgoes.popularmovies.utils.Utility;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String MOVIE_SHARE_HASHTAG = " #PopularMovies";
    private String mMovieStr;
    private Button mFavoriteButton;
    private Movie mMovie;
    private boolean mIsFavorite;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mFavoriteButton = (Button) rootView.findViewById(R.id.favorite);

        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMovie != null) {
                    if (mIsFavorite) {
                        int rows = getContext().getContentResolver().delete(MoviesContract.MovieEntry.buildMovieWithIdUri(mMovie.getId()), null, null);

                        if (rows == 1) {
                            //TODO: remove Toast and button
                            Toast.makeText(getContext(), "Movie " + mMovie.getId() + " removed from favorites!", Toast.LENGTH_LONG).show();
                            mIsFavorite = false;
                            mFavoriteButton.setText("Add");
                        }
                    } else {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MoviesContract.MovieEntry._ID, mMovie.getId());
                        contentValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, mMovie.getTitle());
                        contentValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_URL, mMovie.getPosterPath());
                        contentValues.put(MoviesContract.MovieEntry.COLUMN_SYNOPSIS, mMovie.getSynopsis());
                        contentValues.put(MoviesContract.MovieEntry.COLUMN_RATING, mMovie.getVoteAverage());
                        contentValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE, mMovie.getReleaseDate());

                        Uri uri = getContext().getContentResolver().insert(MoviesContract.MovieEntry.CONTENT_URI, contentValues);

                        if (uri != null) {
                            //TODO: remove Toast and button
                            String id = uri.getLastPathSegment();
                            Toast.makeText(getContext(), "Movie " + id + " set as favorite!", Toast.LENGTH_LONG).show();
                            mIsFavorite = true;
                            mFavoriteButton.setText("Remove");
                        }
                    }
                }
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
                    double vote = mMovie.getVoteAverage();
                    String synopsis = mMovie.getSynopsis();

                    //Get all view
                    TextView titleView = (TextView) rootView.findViewById(R.id.title);
                    TextView releaseView = (TextView) rootView.findViewById(R.id.year);
                    ImageView posterView = (ImageView) rootView.findViewById(R.id.poster);
                    TextView voteView = (TextView) rootView.findViewById(R.id.rate);
                    TextView overviewView = (TextView) rootView.findViewById(R.id.synopsis);

                    //Set values to the views
                    mMovieStr = title;
                    titleView.setText(title);
                    releaseView.setText(releaseDate);
                    Glide.with(getContext())
                            .load(Utility.getPosterUrlForMovie(poster))
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(posterView);
                    voteView.setText(Double.toString(vote) + "/10");
                    overviewView.setText(synopsis);

                }
            }
        }

        setFavorite();

        //TODO: remove button
        mFavoriteButton.setText(mIsFavorite ? "Remove" : "Add");

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mMovieStr + MOVIE_SHARE_HASHTAG);
        return shareIntent;
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
                mIsFavorite = movie.moveToFirst();
                movie.close();
            }
        }
    }
}
