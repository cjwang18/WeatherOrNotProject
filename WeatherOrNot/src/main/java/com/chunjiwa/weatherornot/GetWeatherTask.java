package com.chunjiwa.weatherornot;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cjwang on 11/10/13.
 */
class GetWeatherTask extends AsyncTask<String, String, String> {

    private final ProgressBar progress;
    private final TextView text;
    private final LinearLayout weatherLayout;
    private final Context context;

    public GetWeatherTask(final ProgressBar progress, final TextView text, final LinearLayout weather, final Context context) {
        this.progress = progress;
        this.text = text;
        this.weatherLayout = weather;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... uri) {

        try {
            return getWeatherData(uri[0]);
        } catch (IOException e) {
            return "Unable to retrieve data. URI may be invalid.";
        }
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        Log.d("WON", "onPostExecute() - result: " + result);
        // Parse weather JSON
        try {
            JSONObject jObject = new JSONObject(result);
            //text.setText(result);
            JSONObject weather = jObject.getJSONObject("weather");

            String city = weather.getJSONObject("location").getString("@city");
            String region = weather.getJSONObject("location").getString("@region");
            String country = weather.getJSONObject("location").getString("@country");
            String img = weather.getString("img");
            String condText = weather.getJSONObject("condition").getString("@text");
            String condTemp = weather.getJSONObject("condition").getString("@temp");
            String unit = weather.getJSONObject("units").getString("@temperature");
            JSONArray forecast = weather.getJSONArray("forecast");

            Log.d("WON", "onPostExecute() - forecast: " + forecast);

            // Reset weatherLayout
            weatherLayout.removeAllViews();

            // Set default layout width and height parameters
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            TableLayout.LayoutParams tp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams rp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

            // Location - City
            TextView tvCity = new TextView(context);
            tvCity.setLayoutParams(lp);
            tvCity.setText(city);
            tvCity.setTextAppearance(context, R.style.TextViewHeader1);
            tvCity.setGravity(Gravity.CENTER);
            weatherLayout.addView(tvCity);

            // Location - (Region, ) Country
            TextView tvCountry = new TextView(context);
            tvCountry.setLayoutParams(lp);
            if (region.equals("N/A"))
                tvCountry.setText(country);
            else
                tvCountry.setText(region + ", " + country);
            tvCountry.setTextAppearance(context, R.style.TextViewNormal);
            tvCountry.setGravity(Gravity.CENTER);
            weatherLayout.addView(tvCountry);

            // Condition Img
            ImageView ivCond = new ImageView(context);
            ivCond.setLayoutParams(lp);
            new GetBitmap(ivCond).execute(img);
            ivCond.setScaleX(1.5f);
            ivCond.setScaleY(1.5f);
            ivCond.setPadding(0, 20, 0, 20);
            weatherLayout.addView(ivCond);

            // Condition Text
            TextView tvCondText = new TextView(context);
            tvCondText.setLayoutParams(lp);
            tvCondText.setText(condText);
            tvCondText.setTextAppearance(context, R.style.TextViewMedium);
            tvCondText.setGravity(Gravity.CENTER);
            weatherLayout.addView(tvCondText);

            // Condition Temp
            TextView tvCondTemp = new TextView(context);
            tvCondTemp.setLayoutParams(lp);
            tvCondTemp.setText(condTemp + (char) 0x00B0 + unit);
            tvCondTemp.setTextAppearance(context, R.style.TextViewMedium);
            tvCondTemp.setGravity(Gravity.CENTER);
            weatherLayout.addView(tvCondTemp);

            // Forecast Label
            TextView fcLabel = new TextView(context);
            fcLabel.setLayoutParams(lp);
            fcLabel.setText("Forecast");
            //fcLabel.setTextAppearance(context, R.style.TextViewNormal);
            fcLabel.setGravity(Gravity.CENTER);
            fcLabel.setPadding(0, 20, 0, 0);
            weatherLayout.addView(fcLabel);

            // Forecast Table
            TableLayout fcTable = new TableLayout(context);
            fcTable.setLayoutParams(lp);
                // Table Header Row
                TableRow head = new TableRow(context);
                head.setLayoutParams(tp);
                TextView dayH = new TextView(context);
                dayH.setLayoutParams(rp);
                dayH.setText("Day");
                dayH.setTextAppearance(context, R.style.TextViewTableHeader);
                head.addView(dayH);
                TextView condH = new TextView(context);
                condH.setLayoutParams(rp);
                condH.setText("Weather");
            condH.setTextAppearance(context, R.style.TextViewTableHeader);
                head.addView(condH);
                TextView hiH = new TextView(context);
                hiH.setLayoutParams(rp);
                hiH.setText("High");
            hiH.setTextAppearance(context, R.style.TextViewTableHeader);
                head.addView(hiH);
                TextView loH = new TextView(context);
                loH.setLayoutParams(rp);
                loH.setText("Low");
            loH.setTextAppearance(context, R.style.TextViewTableHeader);
                head.addView(loH);
                fcTable.addView(head);
            for (int i=0 ; i<forecast.length() ; i++) {
                // Initialize row
                TableRow row = new TableRow(context);
                row.setLayoutParams(tp);
                // Fill in data
                JSONObject fcDay = forecast.getJSONObject(i);
                // @day
                TextView day = new TextView(context);
                day.setLayoutParams(rp);
                day.setText(fcDay.getString("@day"));
                day.setTextAppearance(context, R.style.TextViewNormal);
                row.addView(day);
                // @text (condition)
                TextView cond = new TextView(context);
                cond.setLayoutParams(rp);
                cond.setText(fcDay.getString("@text"));
                cond.setTextAppearance(context, R.style.TextViewNormal);
                row.addView(cond);
                // @high
                TextView hi = new TextView(context);
                hi.setLayoutParams(rp);
                hi.setText(fcDay.getString("@high") + (char) 0x00B0 + unit);
                hi.setTextAppearance(context, R.style.ForecastHigh);
                row.addView(hi);
                // @low
                TextView lo = new TextView(context);
                lo.setLayoutParams(rp);
                lo.setText(fcDay.getString("@low") + (char) 0x00B0 + unit);
                lo.setTextAppearance(context, R.style.ForecastLow);
                row.addView(lo);
                // Add row to table
                fcTable.addView(row);
            }
            fcTable.setColumnStretchable(0, true);
            fcTable.setColumnStretchable(1, true);
            fcTable.setColumnStretchable(2, true);
            weatherLayout.addView(fcTable);

        } catch (JSONException e) {
            // oops
            Log.d("WON", "onPostExecute() - JSONException ");
            e.printStackTrace();
        }
        // Hide progress activity circle
        progress.setVisibility(View.GONE);
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as an InputStream, which it returns as
    // a string.
    private String getWeatherData(String myurl) throws IOException {
        InputStream is = null;
        String result = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("WON", "The response code is: " + response);
            is = conn.getInputStream();

            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            result = sb.toString();
            return result;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

}
