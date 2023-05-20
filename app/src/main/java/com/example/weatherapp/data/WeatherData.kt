package com.example.weatherapp.data

data class WeatherData(
    val temp: String,
    val feelsLikeTemp: String,
    val weatherIcon : String,
    val current: Current
)

data class WeatherDataDays(
    val data: MutableList<DaysWeatherListType>
)
