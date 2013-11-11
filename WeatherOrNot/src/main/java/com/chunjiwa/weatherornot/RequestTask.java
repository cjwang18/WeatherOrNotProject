package com.chunjiwa.weatherornot;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 * Created by cjwang on 11/10/13.
 */
class RequestTask extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse;
        String response = null;

        try {
            httpResponse = httpClient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = httpResponse.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

            }

        } catch (ClientProtocolException e) {

        } catch (IOException e ) {

        }

        return response;
    }
}
