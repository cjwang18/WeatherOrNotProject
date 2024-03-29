package com.chunjiwa.weatherornot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    // Variables
    private String locationType;
    private String tempUnitSelected;
    private String tempUnitSelectTitle;
    private MenuItem searchMenuItem;
    private boolean queryOnUnitChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        // Initialize Variables
        locationType = null;
        tempUnitSelected = "f";
        tempUnitSelectTitle = getResources().getString(R.string.action_tempUnitSelect_default);
        queryOnUnitChange = false;

        //Log.d("WON", "MainActivity - onCreate()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                // Do nothing
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                handleSearchQuery(query);
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

        MenuItem tempUnitSelect = menu.findItem(R.id.action_tempUnitSelect);
        tempUnitSelect.setTitle(tempUnitSelectTitle);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        switch (id) {
            case R.id.action_tempUnitSelect:
                if (tempUnitSelected.equals("f")) {
                    tempUnitSelected = "c";
                    tempUnitSelectTitle = getResources().getString(R.string.action_tempUnitSelect_c);
                } else {
                    tempUnitSelected = "f";
                    tempUnitSelectTitle = getResources().getString(R.string.action_tempUnitSelect_f);
                }
                invalidateOptionsMenu();
                queryOnUnitChange();
                break;
            case R.id.action_share_to_facebook:
                handleShareToFacebook();
                break;
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
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    /**
     * Handles search query (event) by
     * executing the GetWeather(Async)Task
     */
    private void handleSearchQuery(String query) {
        //Log.d("WON", "MainActivity - handleSearchQuery()");
        WeatherOrNotApplication wonApp = (WeatherOrNotApplication) getApplication();
        wonApp.setLocationQuery(query.trim());
        //Log.d("WON", "User typed in: " + locationQuery);
        //use the query to search your data somehow
        if (isOnline()) {
            // ok to fetch data
            if (determineLocationType(wonApp.getLocationQuery())) {
                // passed validation and location type determination
                // proceed in making request
                //Log.d("WON", "handleSearchQuery() - passed validation and location type determination");
                searchMenuItem.collapseActionView();
                try {
                    String yql = "select * from weather.forecast where u=\"" + tempUnitSelected + "\" and woeid in (select woeid from geo.places where text=\"" + wonApp.getLocationQuery() + "\")";
                    String queryURI = "http://query.yahooapis.com/v1/public/yql?q=" + URLEncoder.encode(yql, "UTF-8") + "&format=json";
                    //Log.d("WON", "handleSearchQuery() - queryURI: " + queryURI);

                    // Background ImageView
                    ViewSwitcher bg = (ViewSwitcher) findViewById(R.id.backgroundImgViewSwitch);

                    // Progress Circle - enable on emulator, disable on device
                    ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
                    progress.setAlpha(0f);
                    progress.setVisibility(View.VISIBLE);
                    progress.bringToFront();
                    progress.animate()
                            .alpha(1f)
                            .setDuration(this.getResources().getInteger(android.R.integer.config_shortAnimTime))
                            .setListener(null);

                    // Weather Layout
                    final LinearLayout weather = (LinearLayout) findViewById(R.id.weatherLayout);
                    if (!queryOnUnitChange) {
                        weather.animate()
                                .alpha(0f)
                                .setDuration(this.getResources().getInteger(android.R.integer.config_shortAnimTime))
                                .setListener(null);
                    }

                    new GetWeatherTask(bg, progress, weather, queryOnUnitChange, this).execute(queryURI);

                    queryOnUnitChange = false;

                } catch (UnsupportedEncodingException e) {
                    Log.e("WON", "handleSearchQuery() - Unsupported Encoding Exception");
                    return;
                }
            } else {
                // did not pass validation
                //Log.e("WON", "handleSearchQuery() - did not pass validation");
            }
        } else {
            // display no connectivity error
            Toast.makeText(getBaseContext(), R.string.no_network_connectivity, Toast.LENGTH_SHORT).show();
            //Log.d("WON", "handleSearchIntent() - no network connectivity");
        }
    }

    /**
     * Handles sharing to Facebook by prompting
     * user to select a share mode and then
     * starting the ShareToFacebookActivity
     */
    private void handleShareToFacebook() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Post to Facebook");
        builder.setItems(R.array.share_to_facebook_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 'which' parameter contains index position of selected item
                //Log.d("WON", "handleShareToFacebook() - share_to_facebook_array[" + which + "]");
                // Optimization: only start new activity if real intent to share
                if (which != 2) {
                    Intent shareToFbIntent = new Intent(MainActivity.this, ShareToFacebookActivity.class);
                    shareToFbIntent.putExtra("which", which);
                    MainActivity.this.startActivity(shareToFbIntent);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Validation function that also
     * determines the location type
     * from the inputted search query
     */
    private boolean determineLocationType(String location) {
        // Don't need to worry if 'location' string is empty; handled by Android

        WeatherOrNotApplication wonApp = (WeatherOrNotApplication) getApplication();

        //String[] spaceSplit = location.split(" ");
        String[] commaSplit = location.split(",");
        if (/*spaceSplit.length == 1 || */commaSplit.length == 1) { // if location.split's length == 1
            // if location is 5 digits (location.length == 5 and location is all numbers)
            if (location.length() == 5 && location.matches("^\\d+$")) {
                // valid ZIP code
                //Log.d("WON", "determineLocationType() - valid ZIP code");
                locationType = "zip";
            } else if (location.length() != 5 && location.matches("^\\d+$")) {
                // invalid ZIP code - too many digits
                //Log.d("WON", "determineLocationType() - invalid ZIP - too many digits");
                wonApp.setLocationQuery(null);
                Toast.makeText(getBaseContext(), R.string.validation_invalidZip, Toast.LENGTH_SHORT).show();
                return false;
            } else if (location.length() == 5 && !location.matches("^\\d+$")) {
                // invalid ZIP code - not all digits
                //Log.d("WON", "determineLocationType() - invalid ZIP - not all digits");
                wonApp.setLocationQuery(null);
                Toast.makeText(getBaseContext(), R.string.validation_invalidZip, Toast.LENGTH_SHORT).show();
                return false;
            } else { // else, invalid zip code or location
                // invalid LOCATION format
                //Log.d("WON", "determineLocationType() - invalid LOCATION format");
                wonApp.setLocationQuery(null);
                Toast.makeText(getBaseContext(), R.string.validation_invalidLocation, Toast.LENGTH_LONG).show();
                return false;
            }
        } else { // else (if location.split's length > 1 => city, region, country format
            // test for illegal characters
            Pattern p1 = Pattern.compile("[\"()*!@#$&=|;:?/]");
            Matcher m1 = p1.matcher(location);
            if (m1.find()) {
                //Log.d("WON", "determineLocationType() - illegal characters detected");
                wonApp.setLocationQuery(null);
                Toast.makeText(getBaseContext(), R.string.validation_illegalCharacters, Toast.LENGTH_LONG).show();
                return false;
            }
            String temp = location.replaceAll("['-]", " ");
            // test for pattern match
            Pattern p2 = Pattern.compile("^(([a-zA-Z.])+(\\s)*)+,(\\s)*(([a-zA-Z.])+(\\s)*)+(,(\\s)*(([a-zA-Z.])+(\\s)*)+)*$");
            Matcher m2 = p2.matcher(temp);
            if (!m2.find()) {
                //Log.d("WON", "determineLocationType() - invalid LOCATION format");
                wonApp.setLocationQuery(null);
                Toast.makeText(getBaseContext(), R.string.validation_invalidLocation, Toast.LENGTH_LONG).show();
                return false;
            }

            locationType = "city";
        }

        return true;
    }

    /**
     * Helper function to check
     * network status of device
     */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Helper function to re-query
     * for weather results if unit
     * was changed with a valid
     * location query
     */
    private void queryOnUnitChange() {
        WeatherOrNotApplication wonApp = (WeatherOrNotApplication) getApplication();
        String locationQuery = wonApp.getLocationQuery();
        if (locationQuery != null) {
            SearchView searchView = (SearchView) searchMenuItem.getActionView();
            queryOnUnitChange = true;
            searchView.setQuery(locationQuery, true);
        }
    }

}
