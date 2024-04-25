package com.shaik.mausam

import android.annotation.SuppressLint
import android.content.ContentValues.TAG

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.util.query
import com.google.gson.Gson
import com.shaik.mausam.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Tag
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


//api key 72e91ff325a97d1352634e6826d36b11
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate((layoutInflater))
    }
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
       // setContentView(R.layout.activity_main)
        setContentView(binding.root)
        fetchWeatherData("delhi")
        SearchCity()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun SearchCity() {
        val searchView =binding.searchView
        searchView.setOnQueryTextListener(object  :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {

                query?.let {
                    fetchWeatherData(it)
                }
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName:String) {

        val retrofit= Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/" +
                    "")
            .build().create(ApiInterface::class.java)
        val response =retrofit.getWeatherData(cityName, "72e91ff325a97d1352634e6826d36b11", "metric")
        response.enqueue(object : Callback<MausamApp> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<MausamApp>, response: Response<MausamApp>) {
                val responseBody=response.body()





                if (response.isSuccessful && responseBody != null){
                    val temperature= responseBody.main.temp.toString()
                    val humidity= responseBody.main.humidity
                    val windspeed=responseBody.wind.speed
                    val sunrise= responseBody.sys.sunrise.toLong()
                    val sunSet=responseBody.sys.sunset.toLong()
                    val seaLevel=responseBody.main.pressure
                    val condition=responseBody.weather.firstOrNull()?.main?:"unknown"
                    val maxTemp=responseBody.main.temp_max
                    val mintemp=responseBody.main.temp_min
                    binding.temperature.text =
                        "$temperature °C"
                    binding.weather.text = condition
                    binding.sunny.text = "Max temp: $maxTemp °C"
                    binding.max.text = "Min temp: $mintemp °C"
                    binding.humidity.text = "$humidity %"
                    binding.wind.text = "$windspeed m/s"
                    binding.sunrise.text = "${time(sunrise)}"
                    binding.sunset.text = "${time(sunSet)}"
                    binding.sea.text = "$seaLevel hpa"
                    binding.conditions.text=condition
                    binding.min.text=dayName(System.currentTimeMillis())
                    binding.day.text= date()
                    binding.cityname.text="$cityName"


                    //  Log.d("TAG", "onResponse: $temperature")
                    changeImageAccordingToweatherCondition(condition)

                }
            }

            override fun onFailure(call: Call<MausamApp>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })

        }

    private fun changeImageAccordingToweatherCondition(condition:String) {
        when (condition){
            "Clear Sky","Sunny","Clear" ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Partly Clouds","Clouds","Overcast","Mist","Foggy" ->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Light Rain","Drizzle","Moderate Rain", "Showers", "Heavy Rain" ->{
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" ->{
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun date(): String {
        val sdf= SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format((Date()))

    }
    private fun time(timestamp:Long): String {
        val sdf= SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))

    }

    fun dayName(timestamp: Long): String{
        val sdf= SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }
}


