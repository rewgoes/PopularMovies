package com.wolfgoes.popularmovies;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movies, container, false);
    }

    private void fetchMovieList() {
        FetchMovieList moviesTask = new FetchMovieList();
        moviesTask.execute();
    }

    private class FetchMovieList extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = FetchMovieList.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... voids) {

            HttpURLConnection urlConnection;
            BufferedReader reader;

            String moviesJsonStr;

            final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String POPULAR_PARAM = "popular";
            final String TOP_RATED_PARAM = "top_rated";
            final String APPID_PARAM = "api_key";

            //TODO: implement SharedPreference to set order condition
            String order = POPULAR_PARAM;

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendPath(order)
                    .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            Log.d(LOG_TAG, builtUri.toString());

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

                Log.d(LOG_TAG, moviesJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            }

            return null;
        }
    }
}
