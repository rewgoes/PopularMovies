package com.wolfgoes.popularmovies.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wolfgoes.popularmovies.BuildConfig;
import com.wolfgoes.popularmovies.R;
import com.wolfgoes.popularmovies.api.MovieApi;
import com.wolfgoes.popularmovies.data.MoviesContract;
import com.wolfgoes.popularmovies.model.Movie;
import com.wolfgoes.popularmovies.network.Controller;
import com.wolfgoes.popularmovies.utils.Utility;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Fragment {@link MoviesFragment} shows all movies organized in a grid.
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Callback<MovieApi.MovieResult> {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();

    private final int MOVIE_LOADER_ID = 1;
    private TextView mEmptyView;
    private ProgressDialog mDialog;

    ArrayAdapter<Movie> mMovieAdapter;
    String mOrder;

    @Override
    //http://stackoverflow.com/questions/14076296/nullable-annotation-usage
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchMovieList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_action_refresh) {
            fetchMovieList(true);
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(getContext(), SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        mDialog = new ProgressDialog(getContext());

        mMovieAdapter = new ImageAdapter(getContext());
        mMovieAdapter.setNotifyOnChange(false);

        mEmptyView = (TextView) rootView.findViewById(R.id.empty_list_view);

        GridView gridView = (GridView) rootView.findViewById(R.id.movies_grid);
        gridView.setAdapter(mMovieAdapter);
        gridView.setEmptyView(mEmptyView);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getContext(), DetailActivity.class)
                        .putExtra("movie", mMovieAdapter.getItem(position));
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void fetchMovieList() {
        fetchMovieList(false);
    }

    private void fetchMovieList(boolean forceUpdate) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String order = prefs.getString(getString(R.string.pref_order_key), getString(R.string.pref_order_popular));

        if (TextUtils.equals(order, getString(R.string.pref_order_favorites))) {
            mOrder = order;
            getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
            mEmptyView.setText(getString(R.string.empty_favorite_list_view));
        } else {
            getLoaderManager().destroyLoader(MOVIE_LOADER_ID);
            if (TextUtils.isEmpty(mOrder) || !mOrder.equals(order) || forceUpdate) {
                mOrder = order;

                Controller controller = new Controller();
                Retrofit retrofit = controller.getRetrofit();

                MovieApi movieApi = retrofit.create(MovieApi.class);

                Call<MovieApi.MovieResult> call = movieApi.loadMovies(mOrder, Locale.getDefault().getLanguage());
                call.enqueue(this);

                if (mDialog != null) {
                    mDialog.setMessage(getString(R.string.loading_movies));
                    mDialog.show();
                }
            }
            mEmptyView.setText(getString(R.string.empty_list_view));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == MOVIE_LOADER_ID) {
            return new CursorLoader(getContext(),
                    MoviesContract.MovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Movie[] movies = null;

        if (data != null) {
            movies = new Movie[data.getCount()];
            int i = 0;
            while (data.moveToNext()) {
                movies[i] = new Movie();

                movies[i].setId(data.getLong(data.getColumnIndex(MoviesContract.MovieEntry._ID)));
                movies[i].setTitle(data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_TITLE)));
                movies[i].setSynopsis(data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_SYNOPSIS)));
                movies[i].setPosterPath(data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POSTER_URL)));
                movies[i].setBackdropPath(data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_BACKDROP_URL)));
                movies[i].setReleaseDate(data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_RELEASE)).substring(0, 4));
                movies[i].setVoteAverage(data.getDouble(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_RATING)));
                i++;
            }
        }

        mMovieAdapter.clear();
        if (movies != null)
            for (Movie movie : movies) {
                mMovieAdapter.add(movie);
            }
        mMovieAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //do nothing
    }

    @Override
    public void onResponse(Call<MovieApi.MovieResult> call, Response<MovieApi.MovieResult> response) {
        if (response.isSuccessful()) {
            if (mDialog != null && mDialog.isShowing())
                mDialog.dismiss();

            MovieApi.MovieResult changesList = response.body();

            if (changesList != null) {
                List<Movie> movies = changesList.getMovies();

//            if (mDialog != null && mDialog.isShowing())
//                mDialog.dismiss();

                if (movies == null)
                    Log.d(LOG_TAG, "Error: no movies were fetched");
                else {
                    if (BuildConfig.DEBUG)
                        Log.d(LOG_TAG, "Number of movies fetched: " + movies.size());
                    mMovieAdapter.clear();
                    for (Movie movie : movies) {
                        mMovieAdapter.add(movie);
                    }
                    mMovieAdapter.notifyDataSetChanged();
                }
            }
        } else {
            System.out.println(response.errorBody());
        }
    }

    @Override
    public void onFailure(Call<MovieApi.MovieResult> call, Throwable t) {
        t.printStackTrace();
    }

    private class ImageAdapter extends ArrayAdapter<Movie> {
        private Context mContext;
        private LayoutInflater inflater;

        public ImageAdapter(Context context) {
            super(context, R.layout.movie_item, R.id.movies_grid);

            mContext = context;
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.movie_item, parent, false);
            }

            ImageView poster = (ImageView) convertView.findViewById(R.id.poster);

            Glide.with(mContext)
                    .load(Utility.getPosterUrlForMovie(getItem(position).getPosterPath(), null))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(poster);

            return convertView;
        }
    }
}
