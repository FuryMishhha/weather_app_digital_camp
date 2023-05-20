package com.example.weatherapp.view

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.utils.LocalKeyStorage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    lateinit var localKeyStorage: LocalKeyStorage
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->

            if (destination.id == R.id.aboutFragment) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            } else if (destination.id == R.id.feedbackFragment) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            } else if (destination.id == R.id.locationFragment) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            } else if (destination.id == R.id.loginFragment) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }
        setSupportActionBar(topAppBar)
        val actionBar = supportActionBar
        actionBar?.title = " "
        txtlocation.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_locationFragment)
        }
        icsrch.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_locationFragment)
        }
        localKeyStorage = LocalKeyStorage(this)
        binding.txtlocation.text = localKeyStorage.getValue(LocalKeyStorage.cityName)

        val drawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawerLayout,
            topAppBar,
            (R.string.open),
            (R.string.close)
        ) {

        }
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.about -> {
                navController.navigate(R.id.action_homeFragment_to_aboutFragment)
                drawerLayout.close()
            }

            R.id.license -> {
                startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            }

            R.id.feedback -> {
                navController.navigate(R.id.action_homeFragment_to_feedbackFragment)
                drawerLayout.close()
            }
            R.id.login ->{
                navController.navigate(R.id.action_homeFragment_to_loginFragment)
                drawerLayout.close()
            }
        }
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}