package com.nammahasiru.app.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await

class LocationHelper(private val context: Context) {

    private val fused by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    suspend fun lastKnownLatLng(): LatLng? {
        if (!hasPermission()) return null
        val loc = fused.lastLocation.await() ?: return null
        return LatLng(loc.latitude, loc.longitude)
    }
}
