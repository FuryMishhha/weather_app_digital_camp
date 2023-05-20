package com.example.weatherapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.databinding.FragmentTodayBinding
import com.example.weatherapp.utils.InternetConnectivity
import com.example.weatherapp.utils.LocalKeyStorage
import com.example.weatherapp.viewModel.MainViewModel
import com.google.android.gms.location.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_today.currentIcon
import java.text.SimpleDateFormat
import java.util.*

class TodayFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    lateinit var localKeyStorage: LocalKeyStorage
    private var mLastLocation: Location? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    lateinit var cityName: String
    private val ctx = this

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentTodayBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_today, container, false)
        val view = binding.root

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        localKeyStorage = LocalKeyStorage(requireContext())
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (InternetConnectivity.isNetworkAvailable(requireContext())) {
            val lat = localKeyStorage.getValue("latitude")
            val lon = localKeyStorage.getValue("longitude")
            getAndSetData(binding, lat, lon)
            viewModel.isInternet(true)
        } else {
            viewModel.isInternet(false)
            Toast.makeText(context, "Нет интернет-соединения", Toast.LENGTH_LONG).show()
        }
        requireActivity().findViewById<ImageView>(R.id.myLocationBtn).setOnClickListener {
            getMyLocation()
        }

        return view
    }

    private fun getAndSetData(
        binding: FragmentTodayBinding,
        lat: String?,
        lon: String?
    ) {
        viewModel.getWeatherHourly(lat, lon).observe(viewLifecycleOwner, Observer {
            binding.currentTemp.text = it.temp
            binding.feelsLikeTemp.text = "Ощущается как ${it.feelsLikeTemp}°C"
            binding.climaticConditions.text =
                "Погодные условия: ${it.current.weather.get(id).description}"
            binding.sunriseData.text = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(
                Date((it.current.sunrise).toLong() * 1000)
            )
            binding.sunsetData.text = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(
                Date((it.current.sunset).toLong() * 1000)
            )
            binding.pressureData.text = "${(it.current.pressure * 0.75).toInt()} мм рт.ст"
            binding.humidityData.text = "${it.current.humidity}%"
            binding.windData.text = "${it.current.wind_speed} м/сек"
            binding.cloudiness.text = "${it.current.clouds}%"
            binding.visible.text = "${it.current.visibility} м"
            val weatherImage: ImageView = currentIcon
            Glide.with(requireContext()).load(it.weatherIcon).into(weatherImage)
        })
    }

    @SuppressLint("CutPasteId", "UseCompatLoadingForDrawables")
    override fun onStart() {
        super.onStart()
        requireActivity().findViewById<TextView>(R.id.txtlocation).visibility = View.VISIBLE
        requireActivity().findViewById<ImageView>(R.id.icsrch).visibility = View.VISIBLE
        requireActivity().findViewById<ImageView>(R.id.myLocationBtn).visibility = View.VISIBLE
        requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.topAppBar).navigationIcon =
            resources.getDrawable(R.drawable.ic_icon)
        requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.topAppBar).title =
            null
    }

    @SuppressLint("MissingPermission")
    fun getMyLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient!!.lastLocation
                    .addOnCompleteListener { task ->
                        mLastLocation = task.result
                        if (mLastLocation != null) {
                            val lat = mLastLocation!!.latitude.toString()
                            val lon = mLastLocation!!.longitude.toString()
                            viewModel.getCityName(lat, lon).observe(viewLifecycleOwner, Observer {
                                if (it != null) {
                                    cityName = it[0].name
                                    localKeyStorage.saveValue(LocalKeyStorage.latitude, lat)
                                    localKeyStorage.saveValue(LocalKeyStorage.longitude, lon)
                                    localKeyStorage.saveValue(LocalKeyStorage.cityName, cityName)
                                    val view = ctx.requireActivity()
                                        .findViewById<TextView>(R.id.txtlocation)
                                    view.text = localKeyStorage.getValue(LocalKeyStorage.cityName)
                                    findNavController().navigate(R.id.action_homeFragment_self)
                                }
                            })
                        } else {
                            requestNewLocationData()
                        }
                    }
            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setCancelable(false)
                    .setTitle("Хотите включить геолокацию?")
                    .setMessage("Позвольте нам помочь!")
                    .setNeutralButton("Отмена") { dialog, which ->
                    }
                    .setPositiveButton("Ок") { dialog, which ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startForResult.launch(intent)
                    }
                    .show()
            }
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMyLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val lat1 = mLastLocation.latitude.toString()
            val lon1 = mLastLocation.longitude.toString()
            localKeyStorage.saveValue(LocalKeyStorage.latitude, lat1)
            localKeyStorage.saveValue(LocalKeyStorage.longitude, lon1)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    companion object {
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            getMyLocation()
        }
}