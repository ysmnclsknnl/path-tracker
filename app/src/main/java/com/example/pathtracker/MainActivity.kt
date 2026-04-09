package com.example.pathtracker

import android.Manifest
import android.app.ComponentCaller
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.example.pathtracker.location.LocationUpdateHandler
import com.example.pathtracker.location.LocationUpdateHandler.Companion.REQUEST_CHECK_SETTINGS
import com.example.pathtracker.ui.theme.PathtrackerTheme
import kotlinx.coroutines.flow.StateFlow
import java.util.logging.Logger

class MainActivity : ComponentActivity() {
    private val locationHandler = LocationUpdateHandler(this)
    private val locationPermissionLauncher = registerForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        if (!permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) && !permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        ) {
            Toast.makeText(
                this,
                getString(R.string.location_permission_denied),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val viewModel by viewModels<LocationViewModel>()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PathtrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Location(viewModel.locationState, Modifier.padding(innerPadding))
                }
            }
        }
        locationHandler.requestLocationPermission(locationPermissionLauncher)
        locationHandler.setLocationClient()
        locationHandler.setUpLocationCallBack(locationViewModel = viewModel)
        locationHandler.startLocationUpdatesIfSettingsEnabled()
    }

    override fun onResume() {
        super.onResume()
        locationHandler.startLocationUpdatesIfSettingsEnabled()
    }

    override fun onPause() {
        super.onPause()
        locationHandler.stopLocationUpdates()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)
        Logger.getLogger(this::class.java.name).info("onActivityResult: $requestCode $resultCode")
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) locationHandler.startLocationUpdates()
            else Toast.makeText(this, "Location settings are not satisfied", Toast.LENGTH_SHORT)
                .show()
        }
    }
}

@Composable
fun Location(locationState: StateFlow<List<Location>>, modifier: Modifier = Modifier) {
    Column () {
        locationState.collectAsState().value.distinctBy { it.latitude to it.longitude }.forEach { location ->
            Text(
                text = " latitude ${location.latitude} longitude ${location.longitude}!",
                modifier = modifier
            )
        }
    }
}
