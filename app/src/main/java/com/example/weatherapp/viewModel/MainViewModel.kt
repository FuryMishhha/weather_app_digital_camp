package com.example.weatherapp.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.weatherapp.data.*
import com.example.weatherapp.repositories.WeatherRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel constructor(application: Application) : AndroidViewModel(application) {
    private val repoInstance = WeatherRepository(application)
    lateinit var weather: Call<MainWeather>
    lateinit var response: Call<List<CityName>>
    val weather_hourly = MutableLiveData<WeatherData>()
    val weather_daily = MutableLiveData<WeatherDataDays>()
    var daysData: MutableList<DaysWeatherListType> = mutableListOf()
    val isLoading = MutableLiveData<Boolean>()
    private val isInternet = MutableLiveData<Boolean>()
    val isLoadingMain = MutableLiveData<Boolean>()
    private var apiQueryUnit: String = ""
    var cityName = MutableLiveData<List<CityName>>()

    fun getWeatherHourly(
        lat: String?,
        lon: String?
    ): MutableLiveData<WeatherData> {
        isLoadingMain.value = true
        apiQueryUnit = "metric"
        this.weather = repoInstance.getServicesApiCall(apiQueryUnit, lat, lon)
        weather.enqueue(object : Callback<MainWeather> {
            override fun onResponse(call: Call<MainWeather>, response: Response<MainWeather>) {
                val weather = response.body()
                if (weather != null) {

                    val currentTemp = (weather.current.temp).toInt().toString()
                    val feelsLikeTemp = (weather.current.feels_like).toInt().toString()
                    val icon = weather.current.weather[0].icon
                    val weatherIcon = "https://openweathermap.org/img/wn/$icon@2x.png"

                    weather_hourly.value =
                        WeatherData(
                            "${currentTemp}°C",
                            feelsLikeTemp,
                            weatherIcon,
                            weather.current
                        )
                    isLoadingMain.value = false
                }
            }

            override fun onFailure(call: Call<MainWeather>, t: Throwable) {
                Log.d("Error", "Error in fetching", t)
            }
        })
        return weather_hourly
    }

    fun getSevenDayWeather(
        lat: String?,
        lon: String?
    ): MutableLiveData<WeatherDataDays> {
        isLoading.value = true
        apiQueryUnit = "metric"
        this.weather = repoInstance.getServicesApiCall(apiQueryUnit, lat, lon)
        weather.enqueue(object : Callback<MainWeather> {
            override fun onResponse(call: Call<MainWeather>, response: Response<MainWeather>) {
                val weather = response.body()
                if (weather != null) {

                    val daily = weather.daily
                    for (element in daily) {
                        val date = SimpleDateFormat("EEEE, d MMM").format(
                            Date((element.dt).toLong() * 1000)
                        )
                        val sunrise = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(
                            Date((element.sunrise).toLong() * 1000)
                        )
                        val sunset = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(
                            Date((element.sunset).toLong() * 1000)
                        )
                        val pop = "${(element.pop * 100).toInt()}%"
                        val index2 = element.weather
                        val description = index2[0].description
                        val pressure = "${(element.pressure * 0.75).toInt()} мм рт.ст."
                        val icon = index2[0].icon
                        val weatherIcon = "https://openweathermap.org/img/wn/$icon@2x.png"
                        val weatherDataElement =
                            DaysWeatherListType(
                                date,
                                "$sunrise, $sunset",
                                "${element.humidity}%",
                                "${element.wind_speed} м/сек",
                                pop,
                                "${element.clouds}%",
                                description,
                                weatherIcon,
                                "${element.temp.max.toInt()}°",
                                "${element.temp.min.toInt()}°",
                                pressure
                            )
                        daysData.add(weatherDataElement)
                    }
                    weather_daily.value = WeatherDataDays(daysData)
                    isLoading.value = false
                }
            }

            override fun onFailure(call: Call<MainWeather>, t: Throwable) {
                Log.d("Error", "Error in fetching", t)
            }
        })
        return weather_daily
    }

    fun getCityName(lat: String?, lon: String?): MutableLiveData<List<CityName>> {
        this.response = repoInstance.getCityName(lat, lon)
        response.enqueue(object : Callback<List<CityName>> {
            override fun onResponse(
                call: Call<List<CityName>>,
                response: Response<List<CityName>>
            ) {
                val response = response.body()
                if (response != null) {
                    cityName.value = response
                }
            }

            override fun onFailure(call: Call<List<CityName>>, t: Throwable) {
                Log.d("Error", "Error in fetching", t)
            }
        })
        return cityName
    }

    fun isInternet(b: Boolean) {
        isInternet.value = b
        if (!b) {
            isLoadingMain.value = !b
        }
    }
}