package com.wolfgoes.popularmovies.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.wolfgoes.popularmovies.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class Utility {

    final static String LOG_TAG = Utility.class.getSimpleName();

    final static String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    final static String VIDEO_BASE_URL = "http://img.youtube.com/vi/%s/mqdefault.jpg";

    public  final static String FILE_DIRECTORY = "/Android/data/%s/Files/%s";

    //Create a non instantiable class
    //http://stackoverflow.com/questions/8848107/how-to-construct-a-non-instantiable-and-non-inheritable-class-in-java
    private Utility() {
        throw new RuntimeException("No not try to instantiate this");
    }

    public static String getPosterUrlForMovie(String posterName, String size) {

        if (TextUtils.isEmpty(size)) {
            size = "w185";
        }

        return POSTER_BASE_URL + size + posterName;
    }

    public static String getVideoThumbnail(String posterName) {
        return String.format(VIDEO_BASE_URL, posterName);
    }

    public static String getOrderPreference(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.pref_order_key), context.getString(R.string.pref_order_popular));
    }

    public static String storeImage(Context context, String name, String movieId, Bitmap image) {
        File pictureFile = getOutputMediaFile(context, movieId, name);
        if (pictureFile == null) {
            Log.d(LOG_TAG, "Error creating media file, check storage permissions: ");
            return null;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return pictureFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
        }
        return null;
    }

    private static File getOutputMediaFile(Context context, String name, String movieId) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + String.format(FILE_DIRECTORY, context.getApplicationContext().getPackageName(), movieId)
        );

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + name);
        return mediaFile;
    }

    public static void deleteImages(Context context, String movieId) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + String.format(FILE_DIRECTORY, context.getApplicationContext().getPackageName(), movieId)
        );
        if (mediaStorageDir.isDirectory()) {
            String[] children = mediaStorageDir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(mediaStorageDir, children[i]).delete();
            }
        }
        mediaStorageDir.delete();
    }

    public static void animateView(final View view, final int toVisibility, float toAlpha, int duration) {
        boolean show = toVisibility == View.VISIBLE;
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate()
                .setDuration(duration)
                .alpha(show ? toAlpha : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(toVisibility);
                    }
                });
    }

}
