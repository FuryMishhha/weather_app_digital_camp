package com.example.weatherapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.R
import com.example.weatherapp.adapters.DayWeatherRecyclerAdapter
import com.example.weatherapp.data.DaysWeatherListType
import com.example.weatherapp.databinding.FragmentDaysBinding
import com.example.weatherapp.utils.LocalKeyStorage
import com.example.weatherapp.viewModel.MainViewModel

class DaysFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var dayWeatherAdapter: DayWeatherRecyclerAdapter
    var dayWeatherData: ArrayList<DaysWeatherListType> = ArrayList()
    lateinit var localKeyStorage: LocalKeyStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentDaysBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_days, container, false)
        val view = binding.root
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.lottie.visibility = View.VISIBLE
        localKeyStorage = LocalKeyStorage(requireContext())
        val lat = localKeyStorage.getValue("latitude")
        val lon = localKeyStorage.getValue("longitude")
        viewModel.getSevenDayWeather(lat, lon).observe(viewLifecycleOwner, Observer {

            dayWeatherData = it.data as ArrayList<DaysWeatherListType>
            binding.daysRV.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                dayWeatherAdapter = DayWeatherRecyclerAdapter(requireContext(), -1)
                adapter = dayWeatherAdapter
                dayWeatherAdapter.setWeather(dayWeatherData)
            }
        })
        return view
    }
}