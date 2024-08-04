// IWeatherService.aidl
package com.gdet.testapp;

import com.gdet.testapp.IWeatherServiceCallback;

// Declare any non-default types here with import statements

interface IWeatherService {
    String getWeather(String city);
    void addCallback(IWeatherServiceCallback callback);
    void removeCallback(IWeatherServiceCallback callback);
}