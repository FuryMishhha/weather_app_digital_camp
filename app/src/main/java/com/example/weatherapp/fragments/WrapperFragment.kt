package com.example.weatherapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.R
import com.example.weatherapp.utils.LocalKeyStorage
import com.example.weatherapp.viewModel.MainViewModel
import com.google.android.gms.location.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer

class WrapperFragment : Fragment() {

    private var mLastLocation: Location? = null
    lateinit var localKeyStorage: LocalKeyStorage
    lateinit var contextView: View
    var cityName: String = ""
    private lateinit var MviewModel: MainViewModel
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val ctx = this

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wrapper, container, false)
        contextView = view.findViewById(R.id.coordinatorLayout)
        MviewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        localKeyStorage = LocalKeyStorage(requireContext())
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLastLocation()
        return view
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled() && (localKeyStorage.getValue(LocalKeyStorage.latitude) == null || localKeyStorage.getValue(
                    LocalKeyStorage.longitude
                ) == null)
            ) {
                mFusedLocationClient!!.lastLocation
                    .addOnCompleteListener { task ->
                        mLastLocation = task.result
                        if (mLastLocation != null) {
                            val lat = mLastLocation!!.latitude.toString()
                            val lon = mLastLocation!!.longitude.toString()
                            MviewModel.getCityName(lat, lon).observe(viewLifecycleOwner, Observer {
                                if (it != null) {
                                    cityName = it[0].name
                                    localKeyStorage.saveValue(LocalKeyStorage.latitude, lat)
                                    localKeyStorage.saveValue(LocalKeyStorage.longitude, lon)
                                    localKeyStorage.saveValue(LocalKeyStorage.cityName, cityName)
                                    val view = ctx.requireActivity()
                                        .findViewById<TextView>(R.id.txtlocation)
                                    view.text = localKeyStorage.getValue(LocalKeyStorage.cityName)
                                    findNavController().navigate(R.id.action_wrapperFragment_to_homeFragment)
                                }
                            })
                        } else {
                            requestNewLocationData()
                        }
                    }
            } else {
                if (localKeyStorage.getValue(LocalKeyStorage.latitude) == null || localKeyStorage.getValue(
                        LocalKeyStorage.longitude
                    ) == null
                ) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setCancelable(false)
                        .setTitle("Хотите включить геолокацию?")
                        .setMessage("Позвольте нам помочь!")
                        .setNeutralButton("Отмена") { dialog, which ->
                            findNavController().navigate(R.id.action_wrapperFragment_to_searchFragment)
                        }
                        .setPositiveButton("Ок") { dialog, which ->
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startForResult.launch(intent)
                        }
                        .show()
                } else {
                    findNavController().navigate(R.id.action_wrapperFragment_to_homeFragment)
                }

            }
        } else {
            if (localKeyStorage.getValue(LocalKeyStorage.latitude) == null || localKeyStorage.getValue(
                    LocalKeyStorage.longitude
                ) == null
            ) {
                requestPermissions()
            } else {
                findNavController().navigate(R.id.action_wrapperFragment_to_homeFragment)
            }
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
                getLastLocation()

            } else {
                findNavController().navigate(R.id.action_wrapperFragment_to_searchFragment)
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

    @SuppressLint("CutPasteId")
    override fun onStart() {
        super.onStart()
        requireActivity().findViewById<TextView>(R.id.txtlocation).visibility = View.GONE
        requireActivity().findViewById<ImageView>(R.id.icsrch).visibility = View.GONE
        requireActivity().findViewById<ImageView>(R.id.myLocationBtn).visibility = View.GONE
        requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.topAppBar).navigationIcon =
            null
        requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.topAppBar).title =
            "Weather App"
        requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.topAppBar)
            .setTitleTextColor(Color.WHITE)

    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            getLastLocation()
        }
}