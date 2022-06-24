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

        setUserNameOnToolbar()

        navigateToTrackingFragmentIfNeeded(intent)

//        setSupportActionBar(binding.toolbar)
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnNavigationItemReselectedListener {
            /* No Operation*/
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment -> {
                    setUserNameOnToolbar()
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
                else -> binding.bottomNavigationView.visibility = View.GONE
            }
        }
    }

    private fun setUserNameOnToolbar() {
        val name = sharedPreferences.getString(Constants.KEY_NAME, "")!!
        if (name.isNotBlank() || name.isNotEmpty()) {
            setToolbarTitle("Hi, $name!")
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
}