package com.wolfgoes.popularmovies.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wolfgoes.popularmovies.R;
import com.wolfgoes.popularmovies.api.MovieApi;
import com.wolfgoes.popularmovies.data.MoviesContract;
import com.wolfgoes.popularmovies.listener.OnLoadMoreListener;
import com.wolfgoes.popularmovies.model.Movie;
import com.wolfgoes.popularmovies.network.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Fragment {@link MoviesFragment} shows all mMovies organized in a grid.
 */
public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Callback<MovieApi.MovieResult> {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();
    public static final String STATE_MOVIE_LIST = "state_movie_list";
    private static final String STATE_MOVIE_ORDER = "extra_movie_order";

    private final int MOVIE_LOADER_ID = 1;
    private TextView mEmptyView;
    private ProgressDialog mDialog;
    private DynamicSpanRecyclerView mRecyclerView;
    private ArrayList<Movie> mMovies;
    private String mOrder;
    private boolean mLoadMore = true;
    private final Object lock = new Object();

    MovieAdapter mMovieAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(getContext(), SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ((MainActivity) getActivity()).saveState(STATE_MOVIE_LIST, mMovies);
        outState.putString(STATE_MOVIE_ORDER, mOrder);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        mDialog = new ProgressDialog(getContext());
        mDialog.setCancelable(false);

        mRecyclerView = (DynamicSpanRecyclerView) rootView.findViewById(R.id.movies_grid);

        mMovieAdapter = new MovieAdapter(getContext(), new ArrayList<Movie>());
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_list_view);

        final GridLayoutManager gridLayoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int spanCount = gridLayoutManager.getSpanCount();
                switch (mMovieAdapter.getItemViewType(position)) {
                    case MovieAdapter.VIEW_TYPE_LOADING:
                        return spanCount;
                    default:
                        return 1;
                }
            }
        });

        mRecyclerView.setAdapter(mMovieAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            mMovies = ((MainActivity) getActivity()).getState(STATE_MOVIE_LIST);

            synchronized (lock) {
                if (mMovies != null && mMovies.size() > 0 && mMovies.get(mMovies.size() - 1) == null) {
                    mMovies.remove(mMovies.size() - 1);
                }
            }

            mOrder = savedInstanceState.getString(STATE_MOVIE_ORDER);

            if (TextUtils.equals(mOrder, getString(R.string.pref_order_favorites))) {
                initLoader();
            } else {
                if (!TextUtils.isEmpty(mOrder)) {
                    //set load more listener for the RecyclerView adapter
                    mMovieAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                        @Override
                        public void onLoadMore() {
                            synchronized (lock) {
                                if (mLoadMore) {
                                    if (mMovies != null) {
                                        mRecyclerView.post(new Runnable() {
                                            public void run() {
                                                mMovies.add(null);
                                                mMovieAdapter.notifyItemInserted(mMovies.size());

                                                int page = (mMovies.size() / 20) + 1;
                                                fetchMovieList(mOrder, page);
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(getContext(), R.string.no_more_movies, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }, mRecyclerView);
                }
            }

            if (mMovies != null) {
                mMovieAdapter.setMovies(mMovies);
            }
        }
    }

    public void initLoader() {
        mMovieAdapter.setOnLoadMoreListener(null, mRecyclerView);

        if (getLoaderManager().getLoader(MOVIE_LOADER_ID) == null) {
            getLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
        }

        mEmptyView.setText(getString(R.string.empty_favorite_list_view));
    }


    public void fetchMovieList(String order, int page) {
        fetchMovieList(order, page, false);
    }

    public void fetchMovieList(String order, int page, boolean orderChanged) {
        mOrder = order;

        if (orderChanged) {
            synchronized (lock) {
                mMovieAdapter.setLoaded();
                mMovieAdapter.setFavorite(false);
                mMovies = new ArrayList<>();

                mMovieAdapter.setMovies(mMovies);
                mMovieAdapter.notifyDataSetChanged();
            }

            //set load more listener for the RecyclerView adapter
            mMovieAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    synchronized (lock) {
                        if (mLoadMore) {
                            if (mMovies != null && mMovies.size() > 0) {
                                mRecyclerView.post(new Runnable() {
                                    public void run() {
                                        mMovies.add(null);
                                        mMovieAdapter.notifyItemInserted(mMovies.size());

                                        int page = (mMovies.size() / 20) + 1;
                                        fetchMovieList(mOrder, page);
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getContext(), getString(R.string.no_more_movies), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }, mRecyclerView);
        }

        getLoaderManager().destroyLoader(MOVIE_LOADER_ID);

        Controller controller = new Controller();
        Retrofit retrofit = controller.getRetrofit();

        MovieApi movieApi = retrofit.create(MovieApi.class);

        Call<MovieApi.MovieResult> call = movieApi.loadMovies(order, Locale.getDefault().getLanguage(), page);
        call.enqueue(this);

        if (mDialog != null && page == 1) {
            mDialog.setMessage(getString(R.string.loading_movies));
            mDialog.show();
        }
        mEmptyView.setText(getString(R.string.empty_list_view));
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
        mMovieAdapter.setFavorite(true);
        mMovies = new ArrayList<>();

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
                mMovies.add(movie);
            }
        }

        mMovieAdapter.setMovies(mMovies);
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
            addMovie(response.body());
        } else {
            if (mMovies != null) {
                if (mMovies.get(mMovies.size() - 1) == null){
                    mMovies.remove(mMovies.size() - 1);
                    mMovieAdapter.notifyItemChanged(mMovies.size());
                }
                Toast.makeText(getContext(), getString(R.string.no_more_movies), Toast.LENGTH_SHORT).show();
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
    }

    public void addMovie(MovieApi.MovieResult changesList) {
        synchronized (lock) {
            if (getActivity() == null || getActivity().isDestroyed()) {
                return;
            }

            dismissProgressDialog();

            if (changesList != null) {
                int previousSize = mMovies == null ? 0 : mMovies.size() - 1;
                int receivedMovies = changesList.getMovies().size();
                if (mMovies == null || mMovies.size() == 0) {
                    mMovies = changesList.getMovies();
                    mMovieAdapter.setMovies(mMovies);
                    mMovieAdapter.notifyDataSetChanged();
                } else {
                    mMovies.remove(mMovies.size() - 1);
                    mMovies.addAll(changesList.getMovies());
                    mMovieAdapter.notifyItemRangeChanged(previousSize, receivedMovies);
                    mMovieAdapter.setLoaded();
                }
            }
        }

        if (mMovieAdapter.getItemCount() > 0) {
            showRecyclerView(true);
        } else {
            showRecyclerView(false);
        }
    }

    @Override
    public void onFailure(Call<MovieApi.MovieResult> call, Throwable t) {
        Log.e(LOG_TAG, "onFailure " + t.toString());

        if (getActivity() == null || getActivity().isDestroyed()) {
            return;
        }

        dismissProgressDialog();

        if (mMovies != null && mMovies.size() > 0) {
            if (mMovies.get(mMovies.size() - 1) == null) {
                mMovies.remove(mMovies.size() - 1);
                mMovieAdapter.notifyItemChanged(mMovies.size());
            }
            Toast.makeText(getContext(), getString(R.string.no_more_movies), Toast.LENGTH_SHORT).show();
        } else {
            showRecyclerView(false);
        }
    }

    private void showRecyclerView(boolean show) {
        mRecyclerView.setVisibility(show ? View.VISIBLE : View.GONE);
        mEmptyView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
