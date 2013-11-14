package com.chunjiwa.weatherornot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cjwang on 11/14/13.
 */
public class GetBitmap extends AsyncTask<String, Void, Bitmap> {

    private final ImageView imageView;

    public GetBitmap(final ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... url) {
        try {
            URL bmpUrl = new URL(url[0]);
            return BitmapFactory.decodeStream(bmpUrl.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            Log.d("WON", "GetBitmap - doInBackground() - MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("WON", "GetBitmap - doInBackground() - IOException");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bmp) {
        imageView.setImageBitmap(bmp);
    }

}
