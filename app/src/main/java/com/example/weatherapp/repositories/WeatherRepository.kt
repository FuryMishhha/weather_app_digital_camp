package com.example.weatherapp.repositories

import android.app.Application
import com.example.weatherapp.data.CityName
import com.example.weatherapp.data.MainWeather
import com.example.weatherapp.network.RetroService
import retrofit2.Call

class WeatherRepository constructor(val application: Application) {

    fun getServicesApiCall(apiQueryUnit: String, lat: String?, lon: String?): Call<MainWeather> {
        return RetroService.retroInstance.getWeather(apiQueryUnit, lat!!.toFloat(), lon!!.toFloat(), "ru")
    }

    fun getCityName(lat: String?, lon: String?): Call<List<CityName>> {
        return RetroService.retroInstance.getCityName(lat!!.toFloat(), lon!!.toFloat())
    }
}