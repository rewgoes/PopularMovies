package com.wolfgoes.popularmovies.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.wolfgoes.popularmovies.R;
import com.wolfgoes.popularmovies.data.Movie;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_acitivy);

        TextView text = (TextView) findViewById(R.id.texting);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("movie")) {
                Movie movie = intent.getParcelableExtra("movie");
                if (movie != null )
                    text.setText(movie.getTitle());
            }
        }
    }
}
