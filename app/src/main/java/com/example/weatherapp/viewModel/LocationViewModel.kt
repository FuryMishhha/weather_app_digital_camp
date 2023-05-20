package com.example.weatherapp.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.weatherapp.data.SearchLocationsItem
import com.example.weatherapp.repositories.LocationRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationViewModel constructor(application: Application) : AndroidViewModel(application) {
    val repoInstance = LocationRepository(application)
    lateinit var city: Call<List<SearchLocationsItem>>
    val cityName = MutableLiveData<List<SearchLocationsItem>>()
    var isInternet = MutableLiveData<Boolean>()

    fun getCityName(cities: String): MutableLiveData<List<SearchLocationsItem>> {
        this.city = repoInstance.getServicesApiCall(cities)
        city.enqueue(object : Callback<List<SearchLocationsItem>> {

            override fun onResponse(
                call: Call<List<SearchLocationsItem>>,
                response: Response<List<SearchLocationsItem>>
            ) {
                cityName.value = response.body()
            }

            override fun onFailure(call: Call<List<SearchLocationsItem>>, t: Throwable) {
                isInternet.value = false
            }
        })
        return cityName
    }

    fun isInternet(b: Boolean) {
        isInternet.value = b
    }
}


