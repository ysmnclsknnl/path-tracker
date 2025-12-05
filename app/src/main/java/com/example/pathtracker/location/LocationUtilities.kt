package com.example.pathtracker.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
private const val LOCATION_PROVIDER = LocationManager.GPS_PROVIDER
private const val MINIMUM_UPDATE_TIME_IN_MS = 1000L
private const val MINIMUM_UPDATE_DISTANCE_IN_METERS = 10f

/**
 * Method to verify permissions:
 * - [Manifest.permission.ACCESS_FINE_LOCATION]
 * - [Manifest.permission.ACCESS_COARSE_LOCATION]
 */
internal fun Context.isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.ACCESS_FINE_LOCATION
) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.ACCESS_COARSE_LOCATION
) == PackageManager.PERMISSION_GRANTED

internal fun LocationManager.startLocationUpdates(locationListener: LocationListener) {
    requestLocationUpdates(
        LOCATION_PROVIDER,
        MINIMUM_UPDATE_TIME_IN_MS,
        MINIMUM_UPDATE_DISTANCE_IN_METERS,
        locationListener
    )
}