package com.vinuthna.weatherapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit // ✅ Import KTX extension
import com.vinuthna.weatherapp.ui.theme.WeatherAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.vinuthna.weatherapp.network.WeatherService
import com.vinuthna.weatherapp.model.WeatherResponse

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialize SharedPreferences
        val sharedPreferences = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE)

        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherScreen(modifier = Modifier.padding(innerPadding), sharedPreferences)
                }
            }
        }
    }
}

// ✅ Optimized fetchWeather() with SharedPreferences & Loading State
fun fetchWeather(city: String, updateUI: (String, String) -> Unit, sharedPreferences: android.content.SharedPreferences, setLoading: (Boolean) -> Unit) {
    setLoading(true) // ✅ Start loading

    // ✅ Save last searched city using KTX extension
    sharedPreferences.edit {
        putString("lastCity", city)
    }

    val apiKey = "e6cf51644ce4138dee8a615d4095e3df"
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(WeatherService::class.java)
    val call = service.getWeather(city, apiKey)

    call.enqueue(object : Callback<WeatherResponse> {
        override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
            setLoading(false) // ✅ Stop loading

            if (response.isSuccessful && response.body() != null) {
                val weatherData = response.body()
                updateUI("${weatherData!!.main.temp}°C", weatherData.weather[0].description)
            } else {
                updateUI("--", "No weather data found. Try a different city!") // ✅ Improved error message
            }
        }

        override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
            setLoading(false) // ✅ Stop loading
            updateUI("--", "Couldn't connect to the weather service. Please check your internet!") // ✅ Improved error message
        }
    })
}

// ✅ Updated WeatherScreen() with API Optimization & Loading Indicator
@Composable
fun WeatherScreen(modifier: Modifier = Modifier, sharedPreferences: android.content.SharedPreferences) {
    val context = LocalContext.current

    // ✅ Load last searched city when app starts
    val lastCity = sharedPreferences.getString("lastCity", "Hyderabad") ?: "Hyderabad"
    var cityName by remember { mutableStateOf(lastCity) }
    var temperature by remember { mutableStateOf("--") }
    var condition by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(false) } // ✅ Loading state

    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ✅ Title with Theme Color
        Text(
            text = "Weather Forecast",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ✅ City, Temperature, and Condition with Theme Styling
        Text(
            text = "City: $cityName",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Temperature: $temperature°C",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Condition: $condition",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(20.dp))

        // ✅ Optimized TextField (No unnecessary API calls)
        TextField(
            value = cityName,
            onValueChange = { cityName = it }, // ✅ Only updates city name, no fetching
            label = { Text("Enter City Name", style = MaterialTheme.typography.labelLarge) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        // ✅ Styled Button to Refresh Weather with Loading Indicator
        Button(
            onClick = {
                fetchWeather(cityName, { temp, desc ->
                    temperature = temp
                    condition = desc
                }, sharedPreferences, setLoading = { isLoading = it })
            },
            enabled = !isLoading, // ✅ Button disabled while loading
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary) // ✅ Loading spinner
            } else {
                Text(text = "Refresh Weather", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ✅ Styled Button to View Detailed Forecast
        Button(
            onClick = {
                val intent = Intent(context, ForecastActivity::class.java)
                intent.putExtra("cityName", cityName)
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp)
        ) {
            Text(text = "View Detailed Forecast", fontSize = 18.sp)
        }
    }
}