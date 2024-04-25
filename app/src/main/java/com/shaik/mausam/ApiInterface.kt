package com.shaik.mausam

import androidx.core.text.util.LocalePreferences.TemperatureUnit.TemperatureUnits
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("Weather")
    fun getWeatherData(
        @Query("q") city:String,
        @Query("appid") appid: String,
        @Query("units") units: String,
    ) : Call<MausamApp>
}