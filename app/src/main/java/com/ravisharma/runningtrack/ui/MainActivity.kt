package com.ravisharma.runningtrack.ui

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.ravisharma.runningtrack.R
import com.ravisharma.runningtrack.databinding.ActivityMainBinding
import com.ravisharma.runningtrack.other.Constants
import com.ravisharma.runningtrack.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.ravisharma.runningtrack.other.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        requestPermissions()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        val name = sharedPreferences.getString(Constants.KEY_NAME, "")!!
        if (name.isNotBlank() || name.isNotEmpty()) {
            binding.appTitle.text = "Hi, $name!"
        }

        navigateToTrackingFragmentIfNeeded(intent)

//        setSupportActionBar(binding.toolbar)
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnNavigationItemReselectedListener {
            /* No Operation*/
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment -> {
                    if (name.isNotBlank() || name.isNotEmpty()) {
                        setToolbarTitle("Hi, $name!")
                    }
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
                else -> binding.bottomNavigationView.visibility = View.GONE
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navController.navigate(R.id.action_global_tracking_fragment)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (binding.appTitle.visibility == View.GONE) {
            binding.appTitle.visibility = View.VISIBLE
        }
    }

    fun showHideAppBarLayout(show: Boolean) {
        if (show) {
            binding.appTitle.visibility = View.VISIBLE
        } else {
            binding.appTitle.visibility = View.GONE
        }
    }

    fun setToolbarTitle(title: String) {
        binding.appTitle.text = title
    }
//
//    @RequiresApi(Build.VERSION_CODES.N)
//    val locationPermissionRequest = registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//        when {
//            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
//                // Precise location access granted.
//            }
//            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
//                // Only approximate location access granted.
//            }
//            else -> {
//                // No location access granted.
//            }
//        }
//    }
//
//    private fun requestPermissions() {
//        locationPermissionRequest.launch(arrayOf(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION))
//    }
}