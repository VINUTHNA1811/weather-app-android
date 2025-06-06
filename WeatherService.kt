package com.vinuthna.weatherapp.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import com.vinuthna.weatherapp.model.WeatherResponse

interface WeatherService {
    @GET("weather")
    fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Call<WeatherResponse>
}
