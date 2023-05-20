package com.example.weatherapp.Interface

import com.example.weatherapp.data.SearchLocationsItem
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val BASEURL: String = "https://api.openweathermap.org/geo/1.0/"
const val userapi: String = "de4302130ac8b3e82e43d405cfd2c334"

interface LocationInterface {

    @GET("direct?limit=5&appid=$userapi")
    fun getCity(@Query("q") name: String): Call<List<SearchLocationsItem>>
}

object RetroService {
    val LocationInstance: LocationInterface

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASEURL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        LocationInstance = retrofit.create(LocationInterface::class.java)
    }
}
