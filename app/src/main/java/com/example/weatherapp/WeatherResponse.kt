package com.example.weatherapp

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.google.gson.annotations.SerializedName

    data class WeatherModel(
        @SerializedName("coord") val coord: Coord,
        @SerializedName("weather") val weather: List<Weather>,
        @SerializedName("base") val base: String,
        @SerializedName("main") val main: Main,
        @SerializedName("visibility") val visibility: Int,
        @SerializedName("wind") val wind: Wind,
        @SerializedName("clouds") val clouds: Clouds,
        @SerializedName("dt") val dt: Long,
        @SerializedName("sys") val sys: Sys,
        @SerializedName("timezone") val timezone: Int,
        @SerializedName("id") val id: Int,
        @SerializedName("name") var name: String,
        @SerializedName("cod") val cod: Int
    ) {
        data class Coord(
            @SerializedName("lon") val lon: Double,
            @SerializedName("lat") val lat: Double
        )

        data class Weather(
            @SerializedName("id") val id: Int,
            @SerializedName("main") val main: String,
            @SerializedName("description") val description: String,
            @SerializedName("icon") val icon: String
        )

        data class Main(
            @SerializedName("temp") val temp: Double,
            @SerializedName("feels_like") val feelsLike: Double,
            @SerializedName("temp_min") val tempMin: Double,
            @SerializedName("temp_max") val tempMax: Double,
            @SerializedName("pressure") val pressure: Int,
            @SerializedName("humidity") val humidity: Int
        )

        data class Wind(
            @SerializedName("speed") val speed: Double,
            @SerializedName("deg") val deg: Int
        )

        data class Clouds(
            @SerializedName("all") val all: Int
        )

        data class Sys(
            @SerializedName("type") val type: Int,
            @SerializedName("id") val id: Int,
            @SerializedName("country") val country: String,
            @SerializedName("sunrise") val sunrise: Long,
            @SerializedName("sunset") val sunset: Long
        )

        fun getFormattedDateTime(): String {
            val currentTimeMillis = System.currentTimeMillis()
            // Create a Date object from the current time
            val date = Date(currentTimeMillis)
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) // Define date format
            sdf.timeZone = TimeZone.getDefault() // Set timezone to default
            return sdf.format(date) // Format date and return as string
        }

//    fun getSunriseDateTime(): String {
//        val date = Date(sys.sunrise * 1000L) // Convert seconds to milliseconds
//        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
//        sdf.timeZone = TimeZone.getTimeZone(id.toString()) // Set timezone to default
//        return sdf.format(date) // Format date and return as string
//    }
//
//    fun getSunsetDateTime(): String {
//        val date = Date(sys.sunset * 1000L) // Convert seconds to milliseconds
//        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
//        sdf.timeZone = TimeZone.getTimeZone(id.toString()) // Set timezone to default
//        return sdf.format(date) // Format date and return as string
//    }
}
