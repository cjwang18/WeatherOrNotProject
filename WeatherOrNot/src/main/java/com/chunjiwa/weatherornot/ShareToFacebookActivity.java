package com.chunjiwa.weatherornot;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.WebDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class ShareToFacebookActivity extends Activity {

    // Constants
    private final static int POST_CURRENT_WEATHER = 0;
    private final static int POST_WEATHER_FORECAST = 1;
    private final static int POST_CANCEL = 2;
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    // Variables
    private int which;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_to_facebook);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        Intent intent = getIntent();
        which = intent.getIntExtra("which", POST_CANCEL);
        //Log.d("WON", "ShareToFacebookActivity - onCreate(), which=" + which);

        /*// Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.chunjiwa.weatherornot",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }*/

        // start Facebook Login
        Session.openActiveSession(this, true, new Session.StatusCallback() {

            // callback when session changes state
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (session.isOpened()) {
                    if (which == POST_CANCEL)
                        finishActivity();
                    else
                        performPublish();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.share_to_facebook, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_share_to_facebook, container, false);
            return rootView;
        }
    }

    /**
     * Helper function that ends the activity
     */
    private void finishActivity() {
        this.finish();
    }

    /**
     * Helper function to check for publish permissions
     */
    private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }

    /**
     * Initializes publish action
     */
    private void performPublish() {
        Session session = Session.getActiveSession();
        if (session != null) {
            if (hasPublishPermission()) {
                // We can do the action right away.
                postFeedDialog();
                return;
            } else if (session.isOpened()) {
                // We need to get new permissions, then complete the action when we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(ShareToFacebookActivity.this, PERMISSIONS));
                return;
            }
        }
    }

    /**
     * Build the feed from weatherJSON
     * stored in application state
     */
    private Bundle buildFeedDialog() {
        WeatherOrNotApplication wonApp = (WeatherOrNotApplication) getApplication();
        JSONObject weather = wonApp.getWeatherJSON();
        //Log.d("WON", "buildFeedDialog() - weatherJSON: " + weather);
        if (weather == null) {
            return null;
        }
        try {
            String city = weather.getJSONObject("location").getString("@city");
            String region = weather.getJSONObject("location").getString("@region");
            String country = weather.getJSONObject("location").getString("@country");
            String feed = weather.getString("feed");
            String link = weather.getString("link");
            String img = weather.getString("img");
            String condText = weather.getJSONObject("condition").getString("@text");
            String condTemp = weather.getJSONObject("condition").getString("@temp");
            String unit = weather.getJSONObject("units").getString("@temperature");
            JSONArray forecast = weather.getJSONArray("forecast");

            Bundle params = new Bundle();

            // Common Parameters
            if (region.equals("N/A"))
                params.putString("name", city + ", " + country);
            else
                params.putString("name", city + ", " + region + ", " + country);
            params.putString("link", feed);
            JSONObject properties = new JSONObject();
            JSONObject prop1 = new JSONObject();
            prop1.put("text", "here");
            prop1.put("href", link);
            properties.put("Look at details", prop1);
            params.putString("properties", properties.toString());

            // Post-specific Parameters
            switch (which) {
                case POST_CURRENT_WEATHER:
                    params.putString("picture", img);
                    params.putString("caption", "The current condition for " + city + " is " + condText + ".");
                    params.putString("description", "Temperature is " + condTemp + (char) 0x00B0 + unit + ".");
                    break;
                case POST_WEATHER_FORECAST:
                    params.putString("picture", "http://www-scf.usc.edu/~csci571/2013Fall/hw8/weather.jpg");
                    params.putString("caption", "Weather Forecast for " + city);
                    String description = new String();
                    for (int i=0 ; i<forecast.length() ; i++) {
                        JSONObject fcDay = forecast.getJSONObject(i);
                        description += fcDay.getString("@day") + ": ";
                        description += fcDay.getString("@text") + ", ";
                        description += fcDay.getString("@high") + "/";
                        description += fcDay.getString("@low") + (char) 0x00B0 + unit;
                        if (i+1 != forecast.length())
                            description += "; ";
                        else
                            description += ".";
                    }
                    params.putString("description", description);
                    break;
            }

            return params;
        } catch (JSONException e) {
            Log.e("WON", "ShareToFacebookActivity - buildFeedDialog() - JSONException");
        }

        return null;
    }

    private void postFeedDialog() {

        Bundle params = buildFeedDialog();

        if (params != null) {
            WebDialog feedDialog = (

                new WebDialog.FeedDialogBuilder(ShareToFacebookActivity.this,
                        Session.getActiveSession(),
                        params))
                .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values,
                                           FacebookException error) {
                        if (error == null) {
                            // When the story is posted, echo the success
                            // and the post Id.
                            final String postId = values.getString("post_id");
                            if (postId != null) {
                                Toast.makeText(getBaseContext(),
                                        "Posted story, id: " + postId,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // User clicked the Cancel button
                                Toast.makeText(getBaseContext(),
                                        "Publish cancelled",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            // User clicked the "x" button
                            Toast.makeText(getBaseContext(),
                                    "Publish cancelled",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Generic, ex: network error
                            Toast.makeText(getBaseContext(),
                                    "Error posting story",
                                    Toast.LENGTH_SHORT).show();
                        }
                        finishActivity();
                    }

                })
                .build();
            feedDialog.show();
        } else {
            Toast.makeText(getBaseContext(),
                    "No weather data to post",
                    Toast.LENGTH_SHORT).show();
            finishActivity();
        }
    }

}
