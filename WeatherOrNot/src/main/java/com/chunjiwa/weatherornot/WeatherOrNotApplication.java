package com.chunjiwa.weatherornot;

import android.app.Application;

import org.json.JSONObject;

/**
 * Created by cjwang on 11/16/13.
 */
public class WeatherOrNotApplication extends Application {

    // Variables
    private String locationQuery;
    private JSONObject weatherJSON;

    // Getters
    public String getLocationQuery() {
        return locationQuery;
    }
    public JSONObject getWeatherJSON() {
        return weatherJSON;
    }

    // Setters
    public void setLocationQuery(String locationQuery) {
        this.locationQuery = locationQuery;
    }
    public void setWeatherJSON(JSONObject weatherJSON) {
        this.weatherJSON = weatherJSON;
    }
}
