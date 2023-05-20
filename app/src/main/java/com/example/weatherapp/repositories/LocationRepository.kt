package com.example.weatherapp.repositories

import android.app.Application
import com.example.weatherapp.Interface.RetroService
import com.example.weatherapp.data.SearchLocationsItem

import retrofit2.Call

class LocationRepository constructor(val application: Application) {

    fun getServicesApiCall(name : String) : Call<List<SearchLocationsItem>> {
        return RetroService.LocationInstance.getCity(name)
    }
}