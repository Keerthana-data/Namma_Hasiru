package com.nammahasiru.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nammahasiru.app.R
import com.nammahasiru.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* optional */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        maybeRequestNotificationPermission()

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController

        val topLevel = setOf(
            R.id.homeFragment,
            R.id.treeMapFragment,
            R.id.speciesGuideFragment,
        )
        val appBarConfig = AppBarConfiguration(topLevel)
        binding.toolbar.setupWithNavController(navController, appBarConfig)

        findViewById<BottomNavigationView>(R.id.bottom_nav).setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility = when (destination.id) {
                R.id.addPlantFragment, R.id.treeDetailFragment -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val ok = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!ok) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
