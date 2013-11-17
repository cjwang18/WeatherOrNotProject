package com.chunjiwa.weatherornot;

import android.app.Application;

import org.json.JSONObject;

/**
 * Created by cjwang on 11/16/13.
 */
public class WeatherOrNotApplication extends Application {
    private JSONObject weatherJSON;

    public JSONObject getWeatherJSON() {
        return weatherJSON;
    }

    public void setWeatherJSON(JSONObject weatherJSON) {
        this.weatherJSON = weatherJSON;
    }
}
