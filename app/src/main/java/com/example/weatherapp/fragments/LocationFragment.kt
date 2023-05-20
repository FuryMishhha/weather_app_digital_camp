package com.example.weatherapp.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.R
import com.example.weatherapp.data.UserHistoryItem
import com.example.weatherapp.databinding.FragmentLocationBinding
import com.example.weatherapp.utils.InternetConnectivity
import com.example.weatherapp.utils.LocalKeyStorage
import com.example.weatherapp.viewModel.LocationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_about.backbtn
import kotlinx.android.synthetic.main.fragment_location.list_cities
import kotlinx.android.synthetic.main.fragment_location.text_history

class LocationFragment : Fragment() {
    private lateinit var LviewModel: LocationViewModel
    lateinit var binding: FragmentLocationBinding
    lateinit var localKeyStorage: LocalKeyStorage
    private lateinit var myDataBase: DatabaseReference
    private lateinit var myFirebaseAuth: FirebaseAuth
    private lateinit var listCities: ListView
    private lateinit var listData1: ArrayList<String>
    private lateinit var listData2: ArrayList<UserHistoryItem>
    private lateinit var adapter1: ArrayAdapter<String>
    private lateinit var adapter2: ArrayAdapter<UserHistoryItem>
    private var CITY_KEY = "City"

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = myFirebaseAuth.currentUser
        if (currentUser != null) {
            getDataFromDB()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_location, container, false)
        val view = binding.root
        localKeyStorage = LocalKeyStorage(requireContext())
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LviewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        binding.lViewModel = LviewModel
        binding.lifecycleOwner = this
        binding.lottie.visibility = View.GONE

        myFirebaseAuth = FirebaseAuth.getInstance()
        myDataBase = FirebaseDatabase.getInstance().getReference(CITY_KEY)

        backbtn.setOnClickListener {
            findNavController().navigate(R.id.action_locationFragment_to_homeFragment)
        }

        getDataFromDB()

        setOnClickItem()

        if (InternetConnectivity.isNetworkAvailable(requireContext())) {
            LviewModel.isInternet(true)
        } else {
            LviewModel.isInternet(false)
            Toast.makeText(context, "Нет интернет-соединения", Toast.LENGTH_LONG).show()
        }
        binding.searchCity.addTextChangedListener(textWatcher)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            s?.let { str ->
                if (str.isNotEmpty()) {
                    binding.changeBtn.setImageResource(R.drawable.ic_close)
                    binding.changeBtn.setOnClickListener {
                        binding.searchCity.setText("")
                    }
                    if (InternetConnectivity.isNetworkAvailable(requireContext())) {
                        LviewModel.isInternet(true)
                    } else {
                        LviewModel.isInternet(false)
                    }

                    object : CountDownTimer(500, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                        }

                        override fun onFinish() {
                            LviewModel.getCityName(str.toString())
                            LviewModel.cityName.observe(viewLifecycleOwner) {
                                if (!it.isNullOrEmpty()) {
                                    binding.cityNameCard.visibility = View.VISIBLE
                                    binding.cityName.text = it[0].name
                                    binding.cityNameCard.setOnClickListener { lol ->

                                        val currentUser: FirebaseUser? = myFirebaseAuth.currentUser
                                        if (currentUser != null) {
                                            if (currentUser.isEmailVerified) {
                                                it[0].name?.let { it1 ->
                                                    currentUser.email?.let { it2 ->
                                                        saveCity(
                                                            it2,
                                                            it1,
                                                            it[0].lat.toString(),
                                                            it[0].lon.toString()
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        localKeyStorage.saveValue(
                                            LocalKeyStorage.latitude,
                                            it[0].lat.toString()
                                        )
                                        localKeyStorage.saveValue(
                                            LocalKeyStorage.longitude,
                                            it[0].lon.toString()
                                        )
                                        localKeyStorage.saveValue(
                                            LocalKeyStorage.cityName,
                                            it[0].name.toString()
                                        )
                                        val view =
                                            requireActivity().findViewById<TextView>(R.id.txtlocation)
                                        view.text =
                                            localKeyStorage.getValue(LocalKeyStorage.cityName)

                                        findNavController().popBackStack(
                                            R.id.homeFragment,
                                            true
                                        )
                                        findNavController().navigate(R.id.homeFragment)
                                    }
                                } else {
                                    binding.cityNameCard.visibility = View.GONE
                                }
                            }
                        }
                    }.start()
                } else {
                    binding.changeBtn.setImageResource(R.drawable.searchicon)
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    private fun saveCity(email: String, city: String, latitude: String, longitude: String) {
        myDataBase.push().setValue(UserHistoryItem(email, city, latitude, longitude))
    }

    private fun getDataFromDB() {
        val currentUser: FirebaseUser? = myFirebaseAuth.currentUser
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                listCities = list_cities
                listData1 = ArrayList()
                adapter1 =
                    context?.let {
                        ArrayAdapter(
                            it,
                            android.R.layout.simple_list_item_1,
                            listData1
                        )
                    }!!
                listData2 = ArrayList()
                adapter2 =
                    context?.let {
                        ArrayAdapter(
                            it,
                            android.R.layout.simple_list_item_1,
                            listData2
                        )
                    }!!

                listCities.adapter = adapter1

                val valueListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (listData1.size > 0) listData1.clear()
                        if (listData2.size > 0) listData2.clear()
                        for (ds: DataSnapshot in dataSnapshot.children.reversed()) {
                            if (listData1.size == 5) break
                            val userHistoryItem: UserHistoryItem? =
                                ds.getValue(UserHistoryItem::class.java)
                            if (userHistoryItem != null) {
                                val currentUser: FirebaseUser? = myFirebaseAuth.currentUser
                                if (currentUser != null) {
                                    if (currentUser.email == userHistoryItem.userEmail) {
                                        listData1.add(userHistoryItem.cityName)
                                        listData2.add(userHistoryItem)
                                    }
                                }
                            }
                        }
                        adapter1.notifyDataSetChanged()
                        adapter2.notifyDataSetChanged()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                }
                myDataBase.addValueEventListener(valueListener)
            }
        }
    }

    private fun setOnClickItem() {
        val currentUser: FirebaseUser? = myFirebaseAuth.currentUser
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                listCities.setOnItemClickListener { parent, view, position, id ->
                    val userHistoryItem: UserHistoryItem = listData2.get(position)
                    localKeyStorage.saveValue(
                        LocalKeyStorage.latitude,
                        userHistoryItem.latitude
                    )
                    localKeyStorage.saveValue(
                        LocalKeyStorage.longitude,
                        userHistoryItem.longitude
                    )
                    localKeyStorage.saveValue(
                        LocalKeyStorage.cityName,
                        userHistoryItem.cityName
                    )
                    val view = requireActivity().findViewById<TextView>(R.id.txtlocation)
                    view.text = localKeyStorage.getValue(LocalKeyStorage.cityName)

                    findNavController().popBackStack(
                        R.id.homeFragment,
                        true
                    )
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }
    }
}


