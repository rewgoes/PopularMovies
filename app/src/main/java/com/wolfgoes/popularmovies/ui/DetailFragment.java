package com.wolfgoes.popularmovies.ui;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.wolfgoes.popularmovies.R;
import com.wolfgoes.popularmovies.api.ReviewApi;
import com.wolfgoes.popularmovies.api.VideoApi;
import com.wolfgoes.popularmovies.data.MoviesContract;
import com.wolfgoes.popularmovies.model.Movie;
import com.wolfgoes.popularmovies.model.Review;
import com.wolfgoes.popularmovies.model.Video;
import com.wolfgoes.popularmovies.network.Controller;
import com.wolfgoes.popularmovies.utils.Utility;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.wolfgoes.popularmovies.utils.Utility.FILE_DIRECTORY;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String MOVIE_SHARE_HASHTAG = " #PopularMovies";
    private static final int LOADER_REVIEW_ID = 1;
    private static final int LOADER_VIDEO_ID = 2;

    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;

    private String mMovieStr;
    private FloatingActionButton mFavoriteButton;
    private Movie mMovie;
    private boolean mIsFavorite;
    private View mBackdropGradient;
    private AppBarLayout mAppBar;

    private MenuItem mMenuFavorite;

    private boolean mIsCollapsed = true;

    private RecyclerView mReviewView;
    private TextView mEmptyReview;
    private ReviewAdapter mReviewAdapter;

    private RecyclerView mVideoView;
    private TextView mEmptyVideo;
    private VideoAdapter mVideoAdapter;

    private List<Review> mReviews = new ArrayList<>();
    private List<Video> mVideos = new ArrayList<>();

    private Bitmap mPoster;
    private Bitmap mBackdrop;

    private View mProgressOverlay;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        mProgressOverlay = rootView.findViewById(R.id.progress_overlay);

        mAppBar = (AppBarLayout) rootView.findViewById(R.id.app_bar);

        mAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (!mIsCollapsed && mCollapsingToolbarLayout.getScrimVisibleHeightTrigger() - mAppBar.getMeasuredHeight() >= verticalOffset) {
                    mIsCollapsed = true;
                    if (mMenuFavorite != null) {
                        getActivity().invalidateOptionsMenu();
                    }
                } else if (mIsCollapsed && mCollapsingToolbarLayout.getScrimVisibleHeightTrigger() - mAppBar.getMeasuredHeight() < verticalOffset) {
                    mIsCollapsed = false;
                    if (mMenuFavorite != null) {
                        getActivity().invalidateOptionsMenu();
                    }
                }
            }
        });

        mBackdropGradient = rootView.findViewById(R.id.backdrop_gradient);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);

        mFavoriteButton = (FloatingActionButton) rootView.findViewById(R.id.favorite);

        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFavoriteAction();
            }
        });

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra("movie")) {
                mMovie = intent.getParcelableExtra("movie");
                if (mMovie != null) {
                    //Get all values
                    String title = mMovie.getTitle();
                    String releaseDate = mMovie.getReleaseDate();
                    String poster = mMovie.getPosterPath();
                    final String backdrop = mMovie.getBackdropPath();
                    double vote = mMovie.getVoteAverage();
                    String synopsis = mMovie.getSynopsis();

                    //Get all view
                    TextView releaseView = (TextView) rootView.findViewById(R.id.year);
                    final ImageView backdropView = (ImageView) rootView.findViewById(R.id.backdrop);
                    ImageView posterView = (ImageView) rootView.findViewById(R.id.poster);
                    TextView voteView = (TextView) rootView.findViewById(R.id.rate);
                    TextView overviewView = (TextView) rootView.findViewById(R.id.synopsis);

                    //Set values to the views
                    mMovieStr = title;

                    mCollapsingToolbarLayout.setTitle(title);
                    releaseView.setText(releaseDate);

                    voteView.setText(Double.toString(vote) + "/10");

                    if (!TextUtils.isEmpty(synopsis)) {
                        overviewView.setText(synopsis);
                    } else {
                        overviewView.setText(getString(R.string.description_not_available));
                    }

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                    linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    mReviewAdapter = new ReviewAdapter(getContext(), new ArrayList<Review>());
                    mReviewView = (RecyclerView) rootView.findViewById(R.id.reviews);
                    mReviewView.setLayoutManager(linearLayoutManager);
                    mReviewView.setAdapter(mReviewAdapter);
                    mEmptyReview = (TextView) rootView.findViewById(R.id.empty_reviews);

                    LinearLayoutManager videoLayoutManager = new LinearLayoutManager(getContext());
                    videoLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

                    mVideoAdapter = new VideoAdapter(getContext(), new ArrayList<Video>());
                    mVideoView = (RecyclerView) rootView.findViewById(R.id.videos);
                    mVideoView.setLayoutManager(videoLayoutManager);
                    mVideoView.setAdapter(mVideoAdapter);
                    mEmptyVideo = (TextView) rootView.findViewById(R.id.empty_videos);

                    checkIsFavorite();

                    fetchPoster(poster, posterView);

                    fetchBackdrop(backdrop, backdropView);

                    mFavoriteButton.setImageResource(mIsFavorite ? R.drawable.ic_favorite_true : R.drawable.ic_favorite_false);

                    Controller controller = new Controller();
                    Retrofit retrofit = controller.getRetrofit();

                    ReviewApi reviewApi = retrofit.create(ReviewApi.class);
                    Call<ReviewApi.ReviewResult> reviewCall = reviewApi.loadReviews(mMovie.getId());
                    reviewCall.enqueue(new Callback<ReviewApi.ReviewResult>() {
                        @Override
                        public void onResponse(Call<ReviewApi.ReviewResult> call, Response<ReviewApi.ReviewResult> response) {
                            if (response.isSuccessful()) {
                                ReviewApi.ReviewResult reviewResult = response.body();

                                if (reviewResult != null && reviewResult.getReviews().size() > 0) {
                                    mReviews = reviewResult.getReviews();
                                    mReviewAdapter.setReviews(reviewResult.getReviews());
                                    mReviewAdapter.notifyDataSetChanged();
                                    showReview(true);
                                } else {
                                    showReview(false);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ReviewApi.ReviewResult> call, Throwable t) {

                        }
                    });

                    VideoApi videoApi = retrofit.create(VideoApi.class);
                    Call<VideoApi.VideoResult> videoCall = videoApi.loadVideos(mMovie.getId());
                    videoCall.enqueue(new Callback<VideoApi.VideoResult>() {
                        @Override
                        public void onResponse(Call<VideoApi.VideoResult> call, Response<VideoApi.VideoResult> response) {
                            if (response.isSuccessful()) {
                                VideoApi.VideoResult videoResult = response.body();

                                if (videoResult != null && videoResult.getVideos().size() > 0) {
                                    mVideos = videoResult.getVideos();
                                    mVideoAdapter.setVideos(mVideos);
                                    mVideoAdapter.notifyDataSetChanged();
                                    showVideo(true);
                                } else {
                                    showVideo(false);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<VideoApi.VideoResult> call, Throwable t) {

                        }
                    });
                }
            }
        }

        return rootView;
    }

    private void fetchPoster(final String poster, final ImageView posterView) {
        Uri posterPath;
        if (mIsFavorite) {
            posterPath = Uri.fromFile(new File(Environment.getExternalStorageDirectory() +
                    String.format(FILE_DIRECTORY, getContext().getApplicationContext().getPackageName(), mMovie.getId()) + poster));
        } else {
            posterPath = Uri.parse(Utility.getPosterUrlForMovie(poster, "w500"));
        }

        Glide.with(getContext())
                .load(posterPath)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        mPoster = resource;
                        posterView.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);

                        fetchPoster(poster, posterView);
                    }
                });
    }

    private void fetchBackdrop(final String backdrop, final ImageView backdropView) {
        Uri backdropPath;
        if (mIsFavorite) {
            backdropPath = Uri.fromFile(new File(Environment.getExternalStorageDirectory() +
                    String.format(FILE_DIRECTORY, getContext().getApplicationContext().getPackageName(), mMovie.getId()) + backdrop));
        } else {
            backdropPath = Uri.parse(Utility.getPosterUrlForMovie(backdrop, "w780"));
        }

        Glide.with(getContext())
                .load(backdropPath)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .error(R.drawable.ic_date)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        mBackdrop = resource;
                        mBackdropGradient.setVisibility(View.VISIBLE);
                        backdropView.setImageBitmap(resource);
                        dynamicToolbarColor(resource);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        fetchBackdrop(backdrop, backdropView);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setFavoriteAction();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                            .setMessage("Please, access storage access in order to store images")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
                                }
                            })
                            .setCancelable(false)
                            .create();

                    alertDialog.show();
                }
                break;
        }
    }

    private void setFavoriteAction() {
        if (mMovie != null) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            } else {
                StoreAsyncTask storeAsyncTask = new StoreAsyncTask();
                storeAsyncTask.execute();
            }
        }
    }

    private void dynamicToolbarColor(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                float[] hsv = new float[3];
                int color = palette.getVibrantColor(palette.getMutedColor(palette.getDominantColor(getResources().getColor(R.color.colorPrimary))));
                Color.colorToHSV(color, hsv);
                hsv[2] *= 0.8f; // value component
                int darkColor = Color.HSVToColor(hsv);

                mCollapsingToolbarLayout.setContentScrimColor(color);
                mCollapsingToolbarLayout.setStatusBarScrimColor(darkColor);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        mMenuFavorite = menu.findItem(R.id.action_favorite);
        mMenuFavorite.setIcon(mIsFavorite ? R.drawable.ic_favorite_true_white : R.drawable.ic_favorite_false_white);
        mMenuFavorite.setVisible(mIsCollapsed);
        mMenuFavorite.setEnabled(mIsCollapsed);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                actionShare();
                return true;
            case R.id.action_favorite:
                setFavoriteAction();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkIsFavorite() {
        if (mMovie != null) {
            Cursor movie = getContext().getContentResolver().query(
                    MoviesContract.MovieEntry.buildMovieWithIdUri(mMovie.getId()),
                    null,
                    null,
                    null,
                    null
            );

            if (movie != null) {
                getActivity().invalidateOptionsMenu();
                mIsFavorite = movie.moveToFirst();

                if (mIsFavorite) {
                    Cursor reviews = getContext().getContentResolver().query(
                            MoviesContract.ReviewEntry.buildReviewsFromMovieIdUri(mMovie.getId()),
                            null,
                            null,
                            null,
                            null
                    );

                    if (reviews != null && reviews.getCount() > 0) {
                        while (reviews.moveToNext()) {
                            Review review = new Review();
                            review.setId(reviews.getString(reviews.getColumnIndex(MoviesContract.ReviewEntry._ID)));
                            review.setAuthor(reviews.getString(reviews.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_AUTHOR)));
                            review.setContent(reviews.getString(reviews.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_CONTENT)));
                            mReviews.add(review);
                            mReviewAdapter.setReviews(mReviews);
                            mReviewAdapter.notifyDataSetChanged();
                            showReview(true);
                        }
                    } else {
                        showReview(false);
                    }

                    if (reviews != null) {
                        reviews.close();
                    }

                    Cursor videos = getContext().getContentResolver().query(
                            MoviesContract.VideoEntry.buildVideosFromMovieIdUri(mMovie.getId()),
                            null,
                            null,
                            null,
                            null
                    );

                    if (videos != null && videos.getCount() > 0) {
                        while (videos.moveToNext()) {
                            Video video = new Video();
                            video.setId(videos.getString(videos.getColumnIndex(MoviesContract.VideoEntry._ID)));
                            video.setKey(videos.getString(videos.getColumnIndex(MoviesContract.VideoEntry.COLUMN_KEY)));
                            video.setName(videos.getString(videos.getColumnIndex(MoviesContract.VideoEntry.COLUMN_NAME)));
                            video.setSite(videos.getString(videos.getColumnIndex(MoviesContract.VideoEntry.COLUMN_SITE)));
                            mVideos.add(video);
                            mVideoAdapter.setVideos(mVideos);
                            mVideoAdapter.notifyDataSetChanged();
                            showVideo(true);
                        }
                    } else {
                        showVideo(false);
                    }

                    if (videos != null) {
                        videos.close();
                    }
                }

                movie.close();
            }
        }
    }

    private void actionShare() {
        Intent textShareIntent = new Intent(Intent.ACTION_SEND);
        textShareIntent.setType("text/plain");
        textShareIntent.putExtra(Intent.EXTRA_TEXT, mMovieStr + MOVIE_SHARE_HASHTAG);
        if (textShareIntent.resolveActivity(getActivity().getPackageManager()) != null)
            startActivity(Intent.createChooser(textShareIntent, "Share"));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_REVIEW_ID:
                return new CursorLoader(getContext(), MoviesContract.ReviewEntry.buildReviewsFromMovieIdUri(mMovie.getId()),
                        null, null, null, null);
            case LOADER_VIDEO_ID:
                return new CursorLoader(getContext(), MoviesContract.VideoEntry.buildVideosFromMovieIdUri(mMovie.getId()),
                        null, null, null, null);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void showReview(boolean show) {
        mReviewView.setVisibility(show ? View.VISIBLE : View.GONE);
        mEmptyReview.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showVideo(boolean show) {
        mVideoView.setVisibility(show ? View.VISIBLE : View.GONE);
        mEmptyVideo.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    class StoreAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utility.animateView(mProgressOverlay, View.VISIBLE, 0.4f, 200);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mIsFavorite) {
                int rows = getContext().getContentResolver().delete(MoviesContract.MovieEntry.buildMovieWithIdUri(mMovie.getId()), null, null);

                if (rows == 1) {
                    mIsFavorite = false;
                    getActivity().invalidateOptionsMenu();
                }

                Utility.deleteImages(getContext(), Long.toString(mMovie.getId()));
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MoviesContract.MovieEntry._ID, mMovie.getId());
                contentValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, mMovie.getTitle());
                contentValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_URL, mMovie.getPosterPath());
                contentValues.put(MoviesContract.MovieEntry.COLUMN_BACKDROP_URL, mMovie.getBackdropPath());
                contentValues.put(MoviesContract.MovieEntry.COLUMN_SYNOPSIS, mMovie.getSynopsis());
                contentValues.put(MoviesContract.MovieEntry.COLUMN_RATING, mMovie.getVoteAverage());
                contentValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE, mMovie.getReleaseDate());

                Uri uri = getContext().getContentResolver().insert(MoviesContract.MovieEntry.CONTENT_URI, contentValues);

                if (uri != null) {
                    if (mReviews != null) {
                        ContentValues[] reviewValues = new ContentValues[mReviews.size()];
                        int count = 0;
                        for (Review review : mReviews) {
                            reviewValues[count] = new ContentValues();
                            reviewValues[count].put(MoviesContract.ReviewEntry._ID, review.getId());
                            reviewValues[count].put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
                            reviewValues[count].put(MoviesContract.ReviewEntry.COLUMN_CONTENT, review.getContent());
                            reviewValues[count].put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, mMovie.getId());
                            count++;
                        }
                        getContext().getContentResolver().bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, reviewValues);
                    }

                    if (mVideos != null) {
                        ContentValues[] videoValues = new ContentValues[mVideos.size()];
                        int count = 0;
                        for (Video video : mVideos) {
                            videoValues[count] = new ContentValues();
                            videoValues[count].put(MoviesContract.VideoEntry._ID, video.getId());
                            videoValues[count].put(MoviesContract.VideoEntry.COLUMN_KEY, video.getKey());
                            videoValues[count].put(MoviesContract.VideoEntry.COLUMN_NAME, video.getName());
                            videoValues[count].put(MoviesContract.VideoEntry.COLUMN_SITE, video.getSite());
                            videoValues[count].put(MoviesContract.VideoEntry.COLUMN_MOVIE_ID, mMovie.getId());
                            count++;
                        }
                        getContext().getContentResolver().bulkInsert(MoviesContract.VideoEntry.CONTENT_URI, videoValues);
                    }

                    mIsFavorite = true;
                    getActivity().invalidateOptionsMenu();
                }

                if (mPoster != null) {
                    Utility.storeImage(getContext(), Long.toString(mMovie.getId()), mMovie.getPosterPath(), mPoster);
                }

                if (mBackdrop != null) {
                    Utility.storeImage(getContext(), Long.toString(mMovie.getId()), mMovie.getBackdropPath(), mBackdrop);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (mIsFavorite) {
                mFavoriteButton.setImageResource(R.drawable.ic_favorite_true);
            } else {
                mFavoriteButton.setImageResource(R.drawable.ic_favorite_false);
            }

            Utility.animateView(mProgressOverlay, View.GONE, 0, 200);
        }

    }
}
