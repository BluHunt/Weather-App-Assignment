package com.example.weatherapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlin.math.ceil

class WeatherAdapter(private val weatherList: List<WeatherModel>) : RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_weather_location, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weather = weatherList[position]
        holder.bind(weather)
    }

    override fun getItemCount(): Int {
        return weatherList.size
    }

    inner class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.conditionImage)
        private val locationTextView: TextView = itemView.findViewById(R.id.locationNameTextView)
        private val temperatureTextView: TextView = itemView.findViewById(R.id.temperatureTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedItem = weatherList[position]
                    showAlertDialog(itemView.context,clickedItem)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(weather: WeatherModel) {
            Picasso.get()
                .load("https://openweathermap.org/img/wn/${weather.weather[0].icon}@4x.png")
                .into(imageView)

            locationTextView.text = weather.name
            temperatureTextView.text = "${(ceil(weather.main.temp - 273.15).toInt())}°C"
            descriptionTextView.text = weather.weather[0].description
            timeTextView.text = weather.getFormattedDateTime()
        }
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    private fun showAlertDialog(context: Context, item: WeatherModel) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.alertbox, null)
        dialogView.findViewById<TextView>(R.id.locationNameTextView).text = item.name
        dialogView.findViewById<TextView>(R.id.lonlatTextView).text = "lon: ${item.coord.lon} lat: ${item.coord.lat}"
        dialogView.findViewById<TextView>(R.id.mix_max_desc).text = "${item.weather[0].description} ${(ceil(item.main.tempMin - 273.15).toInt())}°C / ${(ceil(item.main.tempMax - 273.15).toInt())}°C"
        dialogView.findViewById<TextView>(R.id.temperatureTextView).text = "${(ceil(item.main.temp - 273.15).toInt())}°C"
        dialogView.findViewById<TextView>(R.id.feelLikeTextView).text = "${(ceil(item.main.feelsLike - 273.15).toInt())}°C"
        dialogView.findViewById<TextView>(R.id.humidityTextView).text = "${item.main.humidity} %"
        dialogView.findViewById<TextView>(R.id.pressureTextView).text = "${item.main.pressure} hPa"
        dialogView.findViewById<TextView>(R.id.windSpeedTextView).text = "${item.wind.speed} mi/h"
        dialogView.findViewById<TextView>(R.id.windDegTextView).text = item.wind.deg.toString()
        dialogView.findViewById<TextView>(R.id.visibilityTextView).text = "${item.visibility/ 1000.0} Km"

        alertDialogBuilder.setView(dialogView)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            // Handle positive button click if needed
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()

        // Set background color
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#303F9F"))) // Change color code as needed

        // Remove the dialogView from its parent if it has one
        (dialogView.parent as? ViewGroup)?.removeView(dialogView)

        alertDialog.show()
    }
}
