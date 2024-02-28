package com.example.weatherapp

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    // API key for weather service
    private val apiKey = "5b33bad694b5d961d31d5a70a4f767f6"

    // FusedLocationProviderClient for location services
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Variables to store current latitude and longitude
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    // List of locations for weather data
    private val locationsList = listOf(
            "My Current Location", "New York", "Singapore", "Mumbai", "Delhi", "Sydney", "Melbourne"
    )

    private val weatherList = mutableListOf<WeatherModel>()
    private var adapter = WeatherAdapter(weatherList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // SwipeRefreshLayout initialization
        val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        // FusedLocationProviderClient initialization
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Checking for location permission
        checkLocationPermission()
        if (isNetworkAvailable()) {
            fetchDataAndUpdateUI()
        }
        locationsList.forEach { location ->
            val savedData = loadDataForLocation(location)
            if(savedData?.name != null){
                weatherList.add(savedData)
                adapter.notifyDataSetChanged()
            }
        }

        if (!isNetworkAvailable()) {
            if (getSharedPreferences(locationsList[2], Context.MODE_PRIVATE).all.isEmpty()) {
                Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show()
            }
        } else {
            fetchDataAndUpdateUI()
        }

        swipeRefreshLayout.setOnRefreshListener {
            if(isNetworkAvailable()){
                if (isLocationEnabled()) {
                    requestLocationUpdates()
                } else {
                    // Optionally, show a message if location is not enabled
                    Toast.makeText(this, "Location is not enabled", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"No Internet",Toast.LENGTH_SHORT).show()
            }
            swipeRefreshLayout.isRefreshing = false
        }

    }

    override fun onResume() {
        super.onResume()
        if(isNetworkAvailable()){
            if (isLocationEnabled()) {
                requestLocationUpdates()
            } else {
                // Optionally, show a message if location is not enabled
                Toast.makeText(this, "Location is not enabled", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this,"No Internet",Toast.LENGTH_SHORT).show()
        }
    }
    

    // Retrofit API service interface
    interface WeatherApiService {
        @GET("weather")
        fun getCurrentWeather(
            @Query("q") city: String,
            @Query("appid") apiKey: String
        ): Call<WeatherModel>

        @GET("weather")
        fun getCurrentWeatherByCoordinates(
            @Query("lat") latitude: Double,
            @Query("lon") longitude: Double,
            @Query("appid") apiKey: String
        ): Call<WeatherModel>
    }

    private fun fetchDataAndUpdateUI() {
        checkPermissions()
        if (isLocationEnabled()) {
            locationsList.forEach { location ->
                if (location == "My Current Location") {
                    fetchWeatherDataByCoordinates(currentLatitude, currentLongitude, apiKey)
                } else {
                    fetchWeatherDataByLocation(location, apiKey)
                }
            }
        }
    }

    // Function to check network availability
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    // Functions related to location and permissions
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun checkLocationPermission() {
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            // Permission already granted, proceed with location retrieval
            requestLocationUpdates()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with location retrieval
                requestLocationUpdates()
            }
        }
    }
    private fun requestLocationUpdates() {
        if (!isLocationEnabled()) {
            AlertDialog.Builder(this)
                .setTitle("Location Services Disabled")
                .setMessage("Please enable location services to use this app.")
                .setPositiveButton("Settings") { _, _ ->
                    // Open location settings when the user clicks on Settings
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.lastLocation?.let { location ->
                    // Handle location updates
                    handleLocationUpdate(location)
                }
            }
        }

        if (checkPermissions()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            if (locationRequest != null) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }

    private fun handleLocationUpdate(location: Location) {
        currentLatitude = location.latitude
        currentLongitude = location.longitude
        if (isNetworkAvailable()) {
            fetchWeatherDataByCoordinates(currentLatitude,currentLongitude, apiKey)
            fetchDataAndUpdateUI()
        }
    }

    // RetrofitClient object for API requests
    object RetrofitClient {
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

        fun create(): WeatherApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(WeatherApiService::class.java)
        }
    }

    private fun saveDataForLocationWithTimestamp(location: String, weatherResponse: WeatherModel) {
        val sharedPreferences = getSharedPreferences(location, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val jsonWeatherResponse = Gson().toJson(weatherResponse)
        editor.putString("weatherData", jsonWeatherResponse)
        editor.apply()
        //Toast.makeText(this,location,Toast.LENGTH_SHORT).show()
    }

    private fun loadDataForLocation(location: String): WeatherModel? {
        val sharedPreferences = getSharedPreferences(location, Context.MODE_PRIVATE)
        val jsonWeatherResponse = sharedPreferences.getString("weatherData", null)
        val weatherResponse = jsonWeatherResponse?.let { Gson().fromJson(it, WeatherModel::class.java) }
        return weatherResponse
    }



    private fun updateItemWithLocation(location: String, newItem: WeatherModel) {
        val existingIndex = weatherList.indexOfFirst { it.name == location }
        //Toast.makeText(this, "Inside $location", Toast.LENGTH_SHORT).show()
        if (existingIndex != -1) {
            weatherList[existingIndex] = newItem
            adapter.notifyItemChanged(existingIndex)
        } else {
            weatherList.add(newItem)
            adapter.notifyDataSetChanged()
        }
    }

    // Functions to fetch weather data using Retrofit By Coordinates
    private fun fetchWeatherDataByCoordinates(latitude: Double, longitude: Double, apiKey: String) {
        val service = RetrofitClient.create()

        service.getCurrentWeatherByCoordinates(latitude, longitude, apiKey)
            .enqueue(object : Callback<WeatherModel> {
                override fun onResponse(
                    call: Call<WeatherModel>,
                    response: Response<WeatherModel>
                ) {
                    if (response.isSuccessful) {
                        val weatherResponse = response.body()
                        if (weatherResponse?.name.takeIf { it == "Globe" } != null) return
                        weatherResponse?.name = "${weatherResponse?.name} \uD83D\uDCCD"
//                        val coord = weatherResponse!!.coord
//                        val weather = weatherResponse.weather
//                        val base = weatherResponse.base
//                        val main = weatherResponse.main
//                        val visibility = weatherResponse.visibility
//                        val wind = weatherResponse.wind
//                        val clouds = weatherResponse.clouds
//                        val dt = weatherResponse.dt
//                        val sys = weatherResponse.sys
//                        val timezone = weatherResponse.timezone
//                        val id = weatherResponse.id
//                        val name = weatherResponse.name
//                        val cod = weatherResponse.cod
//
//
//                        // Update your dataset with the fetched data
//                        val newItem = WeatherModel(coord,
//                            weather,
//                            base,
//                            main,
//                            visibility,
//                            wind,
//                            clouds,
//                            dt,
//                            sys,
//                            timezone,
//                            id,
//                            name,
//                            cod)
                        saveDataForLocationWithTimestamp("My Current Location", weatherResponse!!)
                        updateItemWithLocation(weatherResponse.name, weatherResponse)
                    } else {
                        // Handle unsuccessful response
                        Log.e("Fetch Data", "Unsuccessful response: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<WeatherModel>, t: Throwable) {
                    Log.e("Weather Request", "Failed: ${t.message}")
                }
            })
    }

    private fun fetchWeatherDataByLocation(location: String, apiKey: String) {
        val service = RetrofitClient.create()
        service.getCurrentWeather(location, apiKey)
            .enqueue(object : Callback<WeatherModel> {
                override fun onResponse(
                    call: Call<WeatherModel>,
                    response: Response<WeatherModel>
                ) {
                    if (response.isSuccessful) {
                        val weatherResponse = response.body()
                        saveDataForLocationWithTimestamp(weatherResponse!!.name, weatherResponse)
                        updateItemWithLocation(weatherResponse.name, weatherResponse)

                    } else {
                        // Handle unsuccessful response
                        Log.e("Fetch Data", "Unsuccessful response: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<WeatherModel>, t: Throwable) {
                    Log.e("Weather Request", "Failed: ${t.message}")
                }
            })

    }
}
