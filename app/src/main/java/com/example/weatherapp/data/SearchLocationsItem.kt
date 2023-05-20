package com.example.weatherapp.data

data class SearchLocationsItem(
    val name: String? = "",
    val country: String? = "",
    val lat: Double? = 0.0,
    val localNames: LocalNamesX? = LocalNamesX(),
    val lon: Double? = 0.0
)
