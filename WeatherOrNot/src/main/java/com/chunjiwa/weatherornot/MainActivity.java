package com.chunjiwa.weatherornot;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.SearchView;
import android.widget.Toast;

public class MainActivity extends Activity {

    // Variables
    private String locationType;
    private String tempUnitSelected;
    private String tempUnitSelectTitle;

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

        Log.d("WON", "MainActivity - onCreate()");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("WON", "MainActivity - onNewIntent()");
        handleSearchIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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
            case R.id.fUnitSelect:
                tempUnitSelected = "f";
                tempUnitSelectTitle = getResources().getString(R.string.action_tempUnitSelect_f);
                Toast.makeText(getBaseContext(), "Temp Unit: " + tempUnitSelectTitle, Toast.LENGTH_SHORT).show();
                Log.d("WON", "MainActivity - tempUnitSelected: " + tempUnitSelected);
                invalidateOptionsMenu();
                break;
            case R.id.cUnitSelect:
                tempUnitSelected = "c";
                tempUnitSelectTitle = getResources().getString(R.string.action_tempUnitSelect_c);
                Toast.makeText(getBaseContext(), "Temp Unit: " + tempUnitSelectTitle, Toast.LENGTH_SHORT).show();
                Log.d("WON", "MainActivity - tempUnitSelected: " + tempUnitSelected);
                invalidateOptionsMenu();
                break;
        }
        /*if (id == R.id.action_settings) {
            return true;
        }*/
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
     * Handles search intent (event).
     */
    private void handleSearchIntent(Intent intent) {
        Log.d("WON", "MainActivity - handleSearchIntent()");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY).trim();
            Log.d("WON", "User typed in: " + query);
            //use the query to search your data somehow
            if (determineLocationType(query)) {
                // passed validation and location type determination
                // proceed in making request
            } else {
                // did not pass validation
            }


        }
    }

    private boolean determineLocationType(String location) {
        // Don't need to worry if 'location' string is empty; handled by Android
        // if 'location' doesn't match pattern AND is all numbers

        // if location.split's length == 1
            // if location is 5 digits (location.length == 5 and location is all numbers)
            // else, invalid zip code or location
        // else (if location.split's length > 1 => city, region, country format
            // test for illegal characters
            // test for pattern match

        //String[] spaceSplit = location.split(" ");
        String[] commaSplit = location.split(",");
        if (/*spaceSplit.length == 1 || */commaSplit.length == 1) {
            if (location.length() == 5 && location.matches("^\\d+$")) {
                // valid ZIP code
                Log.d("WON", "determineLocationType() - valid ZIP code");
            } else if (location.length() != 5 && location.matches("^\\d+$")) {
                // invalid ZIP code - too many digits
                //Log.d("WON", "determineLocationType() - invalid ZIP - too many digits");
                Toast.makeText(getBaseContext(), "Please enter a valid 5 digit U.S. ZIP code.", Toast.LENGTH_SHORT).show();
            } else if (location.length() == 5 && !location.matches("^\\d+$")) {
                // invalid ZIP code - not all digits
                //Log.d("WON", "determineLocationType() - invalid ZIP - not all digits");
                Toast.makeText(getBaseContext(), "Please enter a valid 5 digit U.S. ZIP code.", Toast.LENGTH_SHORT).show();
            } else {
                // invalid LOCATION format
                Log.d("WON", "determineLocationType() - invalid LOCATION format");
                Toast.makeText(getBaseContext(), "Please enter a valid location.", Toast.LENGTH_SHORT).show();
            }
        } else {

        }

        return false;
    }

}
