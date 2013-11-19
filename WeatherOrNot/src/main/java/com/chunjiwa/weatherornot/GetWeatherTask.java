package com.chunjiwa.weatherornot;

import android.content.Context;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by cjwang on 11/10/13.
 */
class GetWeatherTask extends AsyncTask<String, String, String> {

    // Variables
    private final ProgressBar progress;
    private final LinearLayout weatherLayout;
    private final Context context;
    private final LinearLayout.LayoutParams lp;
    private final TableLayout.LayoutParams tp;
    private final TableRow.LayoutParams rp;

    /**
     * Constructor
     */
    public GetWeatherTask(final ProgressBar progress, final LinearLayout weather, final Context context) {
        this.progress = progress;
        this.weatherLayout = weather;
        this.context = context;

        // Set default layout width and height parameters
        lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        rp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
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
        if (result == null) {
            displayGeneralErrorInLayout();
        } else {
            try {
                // Convert string into JSONObject
                JSONObject jObject = new JSONObject(result);
                JSONObject weather = jObject.getJSONObject("weather");
                //Log.d("WON", "onPostExecute() - weather JSON: " + weather);

                // Check if returned JSON is valid weather data
                WeatherOrNotApplication wonApp = (WeatherOrNotApplication) context.getApplicationContext();
                if (weather.isNull("error")) {
                    // Store weather JSON into application state
                    wonApp.setWeatherJSON(weather);
                    // Display weather results in layout
                    displayWeatherInLayout(weather);
                } else {
                    // Clear location query in application state
                    wonApp.setLocationQuery(null);
                    // Clear weather JSON in application state
                    wonApp.setWeatherJSON(null);
                    // Display error message in layout
                    displayErrorInLayout(weather);
                }

            } catch (JSONException e) {
                // oops
                Log.e("WON", "onPostExecute() - JSONException ");
                e.printStackTrace();
            }
        }
        // Hide progress activity circle
        progress.setVisibility(View.GONE);
    }

    /**
     * Given a URL, establishes an HttpUrlConnection and retrieves
     * the web page content as an InputStream, which it returns as
     * a string
     */
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
            Log.i("WON", "getWeatherData() - The response code is: " + response);

            if (response == 200) {
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
            } else {
                return null;
            }

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Resets the weather layout by
     * removing all its views
     */
    private void resetWeatherLayout() {
        weatherLayout.removeAllViews();
    }

    /**
     * Displays a generic error message
     * in the UI layout
     */
    private void displayGeneralErrorInLayout() {
        try {
            JSONObject error = new JSONObject();
            error.put("error", "Something went wrong. Please try again.");
            displayErrorInLayout(error);
        } catch (JSONException e) {
            Log.e("WON", "displayGeneralErrorInLayout() - JSONException");
            e.printStackTrace();
        }
    }

    /**
     * Function that takes in a JSONObject,
     * parses it for the error message,
     * and displays it in the UI layout
     */
    private void displayErrorInLayout(JSONObject error) {
        try {
            // Parse error message from JSON
            String msg = error.getString("error");

            // Reset weatherLayout
            resetWeatherLayout();

            // Error - Title
            TextView tvTitle = new TextView(context);
            tvTitle.setLayoutParams(lp);
            tvTitle.setText("Oops...");
            tvTitle.setTextAppearance(context, R.style.TextViewHeader1);
            tvTitle.setGravity(Gravity.CENTER);
            weatherLayout.addView(tvTitle);

            // Error - Msg
            TextView tvMsg = new TextView(context);
            tvMsg.setLayoutParams(lp);
            tvMsg.setText(msg);
            tvMsg.setTextAppearance(context, R.style.TextViewNormal);
            tvMsg.setGravity(Gravity.CENTER);
            weatherLayout.addView(tvMsg);

        } catch (JSONException e) {
            Log.e("WON", "displayErrorInLayout() - JSONException ");
            e.printStackTrace();
        }
    }

    /**
     * Function that displays the weather
     * information in the UI layout
     */
    private void displayWeatherInLayout(JSONObject weather) {
        try {
            String city = weather.getJSONObject("location").getString("@city");
            String region = weather.getJSONObject("location").getString("@region");
            String country = weather.getJSONObject("location").getString("@country");
            String img = weather.getString("img");
            String condText = weather.getJSONObject("condition").getString("@text");
            String condTemp = weather.getJSONObject("condition").getString("@temp");
            String unit = weather.getJSONObject("units").getString("@temperature");
            JSONArray forecast = weather.getJSONArray("forecast");

            //Log.i("WON", "displayWeatherInLayout() - forecast: " + forecast);

            // Reset weatherLayout
            resetWeatherLayout();

            // Location - City
            TextView tvCity = new TextView(context);
            tvCity.setLayoutParams(lp);
            tvCity.setText(city);
            tvCity.setTextAppearance(context, R.style.TextViewHeader1);
            tvCity.setGravity(Gravity.CENTER);
            tvCity.setPadding(0, 0, 0, 10);
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
            LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            imgLp.setMargins(0, 35, 0, 30);
            ivCond.setLayoutParams(imgLp);
            new GetBitmap(ivCond).execute(img);
            ivCond.setScaleX(2.0f);
            ivCond.setScaleY(2.0f);
            weatherLayout.addView(ivCond);

            // Condition Text
            TextView tvCondText = new TextView(context);
            tvCondText.setLayoutParams(lp);
            tvCondText.setText(condText);
            tvCondText.setTextAppearance(context, R.style.TextViewMedium);
            tvCondText.setGravity(Gravity.CENTER);
            tvCondText.setPadding(0, 0, 0, 10);
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
            fcLabel.setTextAppearance(context, R.style.TextViewTableLabel);
            fcLabel.setGravity(Gravity.CENTER);
            fcLabel.setPadding(0, 40, 0, 20);
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
            Log.e("WON", "displayWeatherInLayout() - JSONException ");
            e.printStackTrace();
        }
    }

}
