package com.chunjiwa.weatherornot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

/**
 * Created by CJ Wang on 11/21/13.
 */
public class BlurBitmap extends AsyncTask<Void, Void, Bitmap> {

    private final Context context;
    private final ImageView imageView;
    private final float blurRadius;

    public BlurBitmap(final Context context, final ImageView imageView, final float blurRadius) {
        this.context = context;
        this.imageView = imageView;
        this.blurRadius = blurRadius;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        return createBlurredImage(imageView, blurRadius);
    }

    @Override
    protected void onPostExecute(Bitmap bmp) {
        imageView.setImageBitmap(bmp);
    }

    private Bitmap createBlurredImage (ImageView iv, float radius)
    {
        // Load a clean bitmap and work from that.
        Bitmap originalBitmap = ((BitmapDrawable)iv.getDrawable()).getBitmap();

        // Create another bitmap that will hold the results of the filter.
        Bitmap blurredBitmap;
        blurredBitmap = Bitmap.createBitmap(originalBitmap);

        // Create the Renderscript instance that will do the work.
        RenderScript rs = RenderScript.create(context);

        // Allocate memory for Renderscript to work with
        Allocation input = Allocation.createFromBitmap(rs, originalBitmap);
        Allocation output = Allocation.createTyped(rs, input.getType());

        // Load up an instance of the specific script that we want to use.
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setInput(input);

        // Set the blur radius
        script.setRadius(radius);

        // Start the ScriptIntrinisicBlur
        script.forEach(output);

        // Copy the output to the blurred bitmap
        output.copyTo(blurredBitmap);

        return blurredBitmap;
    }
}
