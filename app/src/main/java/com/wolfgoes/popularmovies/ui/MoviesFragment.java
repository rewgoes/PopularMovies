package com.wolfgoes.popularmovies.ui;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wolfgoes.popularmovies.BuildConfig;
import com.wolfgoes.popularmovies.R;
import com.wolfgoes.popularmovies.data.Movie;
import com.wolfgoes.popularmovies.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

/**
 * Fragment {@link MoviesFragment} shows all movies organized in a grid.
 */
public class MoviesFragment extends Fragment {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();

    ArrayAdapter<Movie> mMovieAdapter;

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
            fetchMovieList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        mMovieAdapter = new ImageAdapter(getContext());
        mMovieAdapter.setNotifyOnChange(false);

        GridView gridView = (GridView) rootView.findViewById(R.id.movies_grid);
        gridView.setAdapter(mMovieAdapter);
        // Inflate the layout for this fragment
        return rootView;
    }

    private void fetchMovieList() {
        FetchMovieList moviesTask = new FetchMovieList();
        moviesTask.execute();
    }

    private class ImageAdapter extends ArrayAdapter<Movie> {
        private Context context;
        private LayoutInflater inflater;

        public ImageAdapter(Context context) {
            super(context, R.layout.movie_item, R.id.movies_grid);

            this.context = context;
            inflater = LayoutInflater.from(this.context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.movie_item, parent, false);
            }

            Glide.with(context)
                    .load(Utility.getPosterUrlForMovie(getItem(position).getPosterPath()))
                    .into((ImageView) convertView);

            return convertView;
        }
    }

    private class FetchMovieList extends AsyncTask<Void, Void, Movie[]> {

        private final String LOG_TAG = FetchMovieList.class.getSimpleName();

        @Override
        protected void onPostExecute(Movie[] movies) {
            if (movies == null)
                Log.d(LOG_TAG, "Error: no movies were fetched");
            else {
                if (BuildConfig.DEBUG) Log.d(LOG_TAG, "Number of movies fetched: " + movies.length);
                mMovieAdapter.clear();
                for (Movie movie : movies) {
                    mMovieAdapter.add(movie);
                }
                mMovieAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected Movie[] doInBackground(Void... voids) {

            //TODO: check if retrofit should be used to fetch data
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesJsonStr;

            Movie[] movies = null;

            final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String POPULAR_PARAM = "popular";
            final String TOP_RATED_PARAM = "top_rated";
            final String API_ID = "api_key";

            //TODO: implement SharedPreference to set order condition
            String order = POPULAR_PARAM;
            if (BuildConfig.DEBUG) {
                order = new Random().nextBoolean() ? POPULAR_PARAM : TOP_RATED_PARAM;
            }

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendPath(order)
                    .appendQueryParameter(API_ID, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            if (BuildConfig.DEBUG) Log.d(LOG_TAG, builtUri.toString());

            try {
                URL url = new URL(builtUri.toString()); //might cause MalformedURLException

                urlConnection = (HttpURLConnection) url.openConnection(); //IOException
                urlConnection.setRequestMethod("GET"); //ProtocolException
                urlConnection.connect(); //IOException

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();

                movies = getMovieDataFromJson(moviesJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Failed to parse Data: ", e);
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return movies;
        }

        private Movie[] getMovieDataFromJson(String moviesJsonStr) throws JSONException {
            final String JKEY_RESULTS = "results";
            final String JKEY_TITLE = "original_title";
            final String JKEY_DATE = "release_date";
            final String JKEY_POSTER = "poster_path";
            final String JKEY_OVERVIEW = "overview";
            final String JKEY_VOTE = "vote_average";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(JKEY_RESULTS);

            int numMovies = moviesArray.length();
            Movie[] movies = new Movie[numMovies];

            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject movie = moviesArray.getJSONObject(i);
                movies[i] = new Movie();

                movies[i].setTitle(movie.getString(JKEY_TITLE));
                movies[i].setOverview(movie.getString(JKEY_OVERVIEW));
                movies[i].setPosterPath(movie.getString(JKEY_POSTER));
                movies[i].setReleaseDate(movie.getString(JKEY_DATE));
                movies[i].setVoteAverage(movie.getDouble(JKEY_VOTE));
            }

            return movies;
        }
    }
}