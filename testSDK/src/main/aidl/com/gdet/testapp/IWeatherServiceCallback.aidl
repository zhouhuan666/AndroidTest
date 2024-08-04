// IWeatherServiceCallback.aidl
package com.gdet.testapp;

// Declare any non-default types here with import statements

interface IWeatherServiceCallback {
   void onWeatherUpdated(String weather);
}