package com.example.weatherapp.data

class UserHistoryItem {
    lateinit var userEmail: String
    lateinit var cityName: String
    lateinit var latitude : String
    lateinit var longitude : String

    constructor()

    constructor(userEmail: String, cityName: String, latitude: String, longitude: String) {
        this.userEmail = userEmail
        this.cityName = cityName
        this.latitude = latitude
        this.longitude = longitude
    }
}