package com.example.weatherapp.data

data class DaysWeatherListType(
    val date: String,
    val sunriset: String,
    val humidity: String,
    val wind_speed_degree: String,
    val pop: String,
    val clouds: String,
    val description: String,
    val weatherIcon: String,
    val maxTemp: String,
    val minTemp: String,
    val pressure: String,
    var expandable: Boolean = false
)
