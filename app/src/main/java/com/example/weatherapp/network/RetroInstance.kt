package com.example.weatherapp.network

import com.example.weatherapp.data.CityName
import com.example.weatherapp.data.MainWeather
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val base_url: String = "https://api.openweathermap.org"
const val api_key: String = "fb47fa398ad290f6e16e655512d6e8d5"

interface RetroInstance {

    @GET("/data/2.5/onecall?appid=$api_key&exclude=minutely")
    fun getWeather(
        @Query("units") unit: String,
        @Query("lat") latitude: Float,
        @Query("lon") longitude: Float,
        @Query("lang") language: String
    ): Call<MainWeather>

    @GET("/geo/1.0/reverse?limit=1&appid=$api_key")
    fun getCityName(
        @Query("lat") latitude: Float, @Query("lon") longitude: Float
    ): Call<List<CityName>>
}

object RetroService {
    val retroInstance: RetroInstance

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(base_url)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retroInstance = retrofit.create(RetroInstance::class.java)
    }
}