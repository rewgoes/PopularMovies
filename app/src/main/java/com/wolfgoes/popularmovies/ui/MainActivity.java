package com.wolfgoes.popularmovies.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.wolfgoes.popularmovies.R;
import com.wolfgoes.popularmovies.utils.Utility;

public class MainActivity extends AppCompatActivity {

    public static final String STATE_MOVIE_ORDER = "extra_movie_order";

    private String mOrder;

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
        if (!TextUtils.isEmpty(order) && (!TextUtils.equals(order, mOrder) || mOrder == null)) {
            MoviesFragment mf = (MoviesFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if ( null != mf ) {
                mf.fetchMovieList(order);
            }
            mOrder = order;
        }
    }
}
