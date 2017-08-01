package com.wolfgoes.popularmovies.ui;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.wolfgoes.popularmovies.R;
import com.wolfgoes.popularmovies.utils.Utility;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String STATE_MOVIE_ORDER = "extra_movie_order";

    private String mOrder;
    private RetainedFragment retainedFragment;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_MOVIE_ORDER, mOrder);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mOrder = savedInstanceState.getString(STATE_MOVIE_ORDER);
        }

        setContentView(R.layout.activity_main);

        retainedFragment = (RetainedFragment) getSupportFragmentManager().findFragmentByTag("retained_fragment");

        if (retainedFragment == null) {
            retainedFragment = new RetainedFragment();
            getSupportFragmentManager().beginTransaction().add(retainedFragment, "retained_fragment").commit();
        }

        if (findViewById(R.id.fragment_container) != null) {
            MoviesFragment moviesFragment = new MoviesFragment();

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, moviesFragment)
                        .commit();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String order = Utility.getOrderPreference(this);
        // update the location in our second pane using the fragment manager

        MoviesFragment mf = (MoviesFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if ( null != mf ) {
            if (TextUtils.equals(order, getString(R.string.pref_order_favorites))) {
                mf.initLoader();
            } else if (!TextUtils.equals(order, mOrder)) {
                mf.fetchMovieList(order, 1, true);
            }
        }
        mOrder = order;
    }

    public <T> T getState(String key) {
        //noinspection unchecked
        return (T) retainedFragment.map.get(key);
    }

    public void saveState(String key, Object value) {
        retainedFragment.map.put(key, value);
    }

    public static class RetainedFragment extends Fragment {

        HashMap<String, Object> map = new HashMap<>();

        @Override public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

    }
}
