package com.shaik.mausam

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.shaik.mausam.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

//api key 72e91ff325a97d1352634e6826d36b11
class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationManager: LocationManager
    private var url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        searchCity()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        getLocation()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    url =
                        "https://api.openweathermap.org/data/2.5/weather?q=$query&appid=72e91ff325a97d1352634e6826d36b11"
                    fetchData()
                }
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }
        })
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )
            return
        }

        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude
            url =
                "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=72e91ff325a97d1352634e6826d36b11"
            fetchData()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        }
    }

    private fun fetchData() {
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.GET, url, null,

            { response ->
                val main = response.getJSONObject("main")
                val weatherArray = response.getJSONArray("weather")
                val weather = weatherArray.getJSONObject(0)

                val weatherData: Weather = Weather(
                    weather.getString("description"),
                    weather.getString("icon"),
                    weather.getInt("id"),
                    weather.getString("main")
                )

                val mainData: Main = Main(
                    main.getDouble("feels_like"),
                    main.getInt("humidity"),
                    main.getInt("pressure"),
                    main.getDouble("temp"),
                    main.getDouble("temp_max"),
                    main.getDouble("temp_min")
                )

                binding.temperature.text = "${convertKelvinToCelsius(mainData.temp)} °C"
                binding.weather.text = weatherData.description
                binding.sunny.text = "Max temp: ${convertKelvinToCelsius(mainData.temp_max)} °C"
                binding.max.text = "Min temp: ${convertKelvinToCelsius(mainData.temp_min)} °C"
                binding.humidity.text = "${mainData.humidity} %"
                binding.wind.text = "${mainData.humidity} m/s"
                binding.sunrise.text = time(response.getJSONObject("sys").getLong("sunrise"))
                binding.sunset.text = time(response.getJSONObject("sys").getLong("sunset"))
                binding.sea.text = "${mainData.pressure} hpa"
                binding.min.text = dayName()
                binding.day.text = date()
                binding.conditions.text=weatherData.description
                binding.cityname.text = response.getString("name")

                changeImageAccordingToweatherCondition(weatherData.main)

                Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
            },
            {
                Toast.makeText(this@MainActivity, "InvalidSearch", Toast.LENGTH_SHORT).show()
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String>? {
                val headers = HashMap<String, String>()
                headers["User-Agent"] = "Mozilla/5.0"
                return headers
            }
        }
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private fun convertKelvinToCelsius(kelvin: Double): String {
        return String.format(Locale.getDefault(), "%.2f", kelvin - 273.15)
    }

    private fun dayName(): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date(timestamp * 1000))
    }

    private fun changeImageAccordingToweatherCondition(condition: String) {
        when (condition) {
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }

            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        url =
            "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=72e91ff325a97d1352634e6826d36b11"
        fetchData()
    }
}