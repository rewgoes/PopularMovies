package com.wolfgoes.popularmovies.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

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

/**
 * Fragment {@link MoviesFragment} shows all movies organized in a grid.
 */
public class MoviesFragment extends Fragment {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();

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
            fetchMovieList();
            return true;
        } if (id == R.id.action_settings) {
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

        mMovieAdapter = new ImageAdapter(getContext());
        mMovieAdapter.setNotifyOnChange(false);

        GridView gridView = (GridView) rootView.findViewById(R.id.movies_grid);
        gridView.setAdapter(mMovieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String movieTitle = mMovieAdapter.getItem(position).getTitle();
                Toast.makeText(getContext(),movieTitle,Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getContext(), DetailActivity.class)
                        .putExtra("movie", mMovieAdapter.getItem(position));
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void fetchMovieList() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mOrder = prefs.getString(getString(R.string.pref_order_key), getString(R.string.pref_order_popular));

        FetchMovieList moviesTask = new FetchMovieList();
        moviesTask.execute();
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

            Glide.with(mContext)
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
            final String API_ID = "api_key";

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendPath(mOrder)
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
            // TODO: check if original_title or title should be fetched
            // original_title shows Japanese characters
            final String JKEY_TITLE = "title";
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
