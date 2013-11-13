package com.chunjiwa.weatherornot;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by cjwang on 11/10/13.
 */
class GetWeatherTask extends AsyncTask<String, String, String> {

    private final ProgressBar progress;
    private final TextView text;

    public GetWeatherTask(final ProgressBar progress, final TextView text) {
        this.progress = progress;
        this.text = text;
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
            text.setText(result);
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
        /*// Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;*/

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

            /*// Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;*/

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
}
