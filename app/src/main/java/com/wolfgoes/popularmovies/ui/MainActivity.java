package com.wolfgoes.popularmovies.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wolfgoes.popularmovies.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //reference: https://developer.android.com/training/basics/fragments/fragment-ui.html

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {
            // Create a new Fragment to be placed in the activity layout
            MoviesFragment moviesFragment = new MoviesFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            //firstFragment.setArguments(getIntent().getExtras());

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, moviesFragment)
                        .commit();
            }
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //http://stackoverflow.com/questions/10303898/oncreateoptionsmenu-calling-super
        //Calling super does not change how Activity/Fragment menu ara shown
        getMenuInflater().inflate(R.menu.maintest, menu);
        return true; //super.onCreateOptionsMenu(menu)
    }
    */
}
