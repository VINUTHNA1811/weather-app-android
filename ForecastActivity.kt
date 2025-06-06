package com.vinuthna.weatherapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import com.vinuthna.weatherapp.network.WeatherService
import com.vinuthna.weatherapp.model.WeatherResponse

class ForecastActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)

        val cityName = intent.getStringExtra("cityName")?.trim() ?: "Unknown"
        val cityTitle = findViewById<TextView>(R.id.cityTitle)
        val temperatureView = findViewById<TextView>(R.id.temperatureView)
        val minTempView = findViewById<TextView>(R.id.minTempView)
        val maxTempView = findViewById<TextView>(R.id.maxTempView)
        val humidityView = findViewById<TextView>(R.id.humidityView)
        val windSpeedView = findViewById<TextView>(R.id.windSpeedView)
        val sunriseView = findViewById<TextView>(R.id.sunriseView)
        val sunsetView = findViewById<TextView>(R.id.sunsetView)
        val conditionView = findViewById<TextView>(R.id.conditionView)
        val backButton = findViewById<Button>(R.id.backButton)

        cityTitle.text = getString(R.string.weather_for, cityName)

        // ✅ Fetch weather data
        val apiKey = "e6cf51644ce4138dee8a615d4095e3df" // Replace with your actual API key
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)
        val call = service.getWeather(cityName, apiKey)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val weatherData = response.body()

                    // ✅ Log API response for debugging
                    Log.d("WeatherAPI", "Received Data: $weatherData")

                    // ✅ Ensure proper formatting for TextViews
                    temperatureView.text = String.format("Temperature: %.1f°C", weatherData!!.main.temp)
                    minTempView.text = String.format("Min Temp: %.1f°C", weatherData.main.temp_min)
                    maxTempView.text = String.format("Max Temp: %.1f°C", weatherData.main.temp_max)
                    humidityView.text = String.format("Humidity: %d%%", weatherData.main.humidity)
                    windSpeedView.text = String.format("Wind Speed: %.1f km/h", weatherData.wind.speed)
                    conditionView.text = String.format("Condition: %s", weatherData.weather[0].description)
                    sunriseView.text = String.format("Sunrise: %s", convertTimestampToTime(weatherData.sys.sunrise))
                    sunsetView.text = String.format("Sunset: %s", convertTimestampToTime(weatherData.sys.sunset))
                } else {
                    Log.e("WeatherAPI", "API Response Failed: ${response.errorBody()?.string()}")
                    temperatureView.text = "Weather data unavailable"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("WeatherAPI", "API Call Failed: ${t.message}")
                temperatureView.text = "Error fetching weather data"
            }
        })

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    // ✅ Helper function to convert timestamp to readable time format
    private fun convertTimestampToTime(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val date = java.util.Date(timestamp * 1000) // Convert seconds to milliseconds
        return sdf.format(date)
    }
}