package com.vinuthna.weatherapp.model

data class WeatherResponse(
    val main: Main,
    val wind: Wind, // ✅ Added missing Wind object
    val weather: List<WeatherCondition>,
    val sys: Sys // ✅ Added missing Sys object for sunrise/sunset times
)

data class Main(
    val temp: Double,
    val temp_min: Double, // ✅ Added min temperature
    val temp_max: Double, // ✅ Added max temperature
    val humidity: Int
)

data class Wind(
    val speed: Double // ✅ Added wind speed
)

data class WeatherCondition(
    val description: String
)

data class Sys(
    val sunrise: Long, // ✅ Added sunrise time
    val sunset: Long  // ✅ Added sunset time
)