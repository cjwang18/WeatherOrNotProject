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
            Log.e("WON", "GetBitmap - doInBackground() - MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("WON", "GetBitmap - doInBackground() - IOException");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bmp) {
        imageView.setImageBitmap(eraseBG(bmp, -1));
    }

    private static Bitmap eraseBG(Bitmap src, int color) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap b = src.copy(Bitmap.Config.ARGB_8888, true);
        b.setHasAlpha(true);

        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < width * height; i++) {
            if (pixels[i] == color) {
                pixels[i] = 0;
            }
        }

        b.setPixels(pixels, 0, width, 0, 0, width, height);

        return b;
    }

}
