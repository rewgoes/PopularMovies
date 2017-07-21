package com.wolfgoes.popularmovies.ui;

import android.app.ProgressDialog;
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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wolfgoes.popularmovies.BuildConfig;
import com.wolfgoes.popularmovies.R;
import com.wolfgoes.popularmovies.api.MovieApi;
import com.wolfgoes.popularmovies.data.MoviesContract;
import com.wolfgoes.popularmovies.model.Movie;
import com.wolfgoes.popularmovies.network.Controller;

import java.io.IOException;
import java.util.ArrayList;
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
    private DynamicSpanRecyclerView mRecyclerView;

    MovieAdapter mMovieAdapter;
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
    public void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
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

        mMovieAdapter = new MovieAdapter(getContext(), new ArrayList<Movie>());
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_list_view);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), GridLayoutManager.DEFAULT_SPAN_COUNT);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (DynamicSpanRecyclerView) rootView.findViewById(R.id.movies_grid);
        mRecyclerView.setAdapter(mMovieAdapter);

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
            //TODO: mOrder is null
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
        List<Movie> movies = new ArrayList<>();

        if (data != null) {
            while (data.moveToNext()) {
                Movie movie = new Movie();

                movie.setId(data.getLong(data.getColumnIndex(MoviesContract.MovieEntry._ID)));
                movie.setTitle(data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_TITLE)));
                movie.setSynopsis(data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_SYNOPSIS)));
                movie.setPosterPath(data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POSTER_URL)));
                movie.setBackdropPath(data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_BACKDROP_URL)));
                movie.setReleaseDate(data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_RELEASE)).substring(0, 4));
                movie.setVoteAverage(data.getDouble(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_RATING)));
                movies.add(movie);
            }
        }

        mMovieAdapter.setMovies(movies);
        mMovieAdapter.notifyDataSetChanged();

        if (mMovieAdapter.getItemCount() > 0) {
            showRecyclerView(true);
        } else {
            showRecyclerView(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //do nothing
    }

    private void dismissProgressDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onResponse(Call<MovieApi.MovieResult> call, Response<MovieApi.MovieResult> response) {
        if (response.isSuccessful()) {
            if (getActivity() == null || getActivity().isDestroyed()) {
                return;
            }

            dismissProgressDialog();

            MovieApi.MovieResult changesList = response.body();

            if (changesList != null) {
                List<Movie> movies = changesList.getMovies();

                if (movies == null)
                    Log.d(LOG_TAG, "Error: no movies were fetched");
                else {
                    if (BuildConfig.DEBUG)
                        Log.d(LOG_TAG, "Number of movies fetched: " + movies.size());
                    mMovieAdapter.setMovies(movies);
                    mMovieAdapter.notifyDataSetChanged();
                }
            }

            if (mMovieAdapter.getItemCount() > 0) {
                showRecyclerView(true);
            } else {
                showRecyclerView(false);
            }
        } else {
            Log.e(LOG_TAG, "onResponse failed!");
            showRecyclerView(false);

            if (response.errorBody() != null) {
                try {
                    Log.e(LOG_TAG, response.errorBody().string());
                } catch (IOException | NullPointerException e) {
                    //do nothing
                }
            }
        }
    }

    @Override
    public void onFailure(Call<MovieApi.MovieResult> call, Throwable t) {
        Log.e(LOG_TAG, "onFailure " + t.toString());

        if (getActivity() == null || getActivity().isDestroyed()) {
            return;
        }

        dismissProgressDialog();

        showRecyclerView(false);
    }

    private void showRecyclerView(boolean show) {
        mRecyclerView.setVisibility(show ? View.VISIBLE : View.GONE);
        mEmptyView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
