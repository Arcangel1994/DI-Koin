package com.example.broadcastreceiver

import android.Manifest
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ktx.BuildConfig
import com.example.broadcastreceiver.data.model.MyLocationEntity
import com.example.broadcastreceiver.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "PermissionRequest"

enum class PermissionRequestType {
    FINE_LOCATION, BACKGROUND_LOCATION
}

@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity(), Callbacks {

    private var permissionRequestType: PermissionRequestType? = null

    private var activityListener: Callbacks? = null

    lateinit var binding: ActivityMainBinding

    private val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
    private val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56

    private val fineLocationRationalSnackbar by lazy {
        Snackbar.make(
            binding.root,
            R.string.fine_location_permission_rationale,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.ok){
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private val backgroundRationalSnackbar by lazy {
        Snackbar.make(
            binding.root,
            R.string.background_location_permission_rationale,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.ok) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
    }

    private val fusedLocationClient: FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(applicationContext) }
    /*private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = TimeUnit.SECONDS.toMillis(300)
        fastestInterval = TimeUnit.SECONDS.toMillis(250)
        maxWaitTime = TimeUnit.MINUTES.toMillis(6)
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }*/

    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = TimeUnit.SECONDS.toMillis(60)
        fastestInterval = TimeUnit.SECONDS.toMillis(30)
        maxWaitTime = TimeUnit.MINUTES.toMillis(2)
        priority = Priority.PRIORITY_HIGH_ACCURACY
    }



    /*private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = TimeUnit.SECONDS.toMillis(60)
        fastestInterval = TimeUnit.SECONDS.toMillis(30)
        maxWaitTime = TimeUnit.MINUTES.toMillis(2)
        priority = Priority.PRIORITY_HIGH_ACCURACY
    }*/

    private val locationUpdatePendingIntent: PendingIntent by lazy {
        val intent = Intent(this@MainActivity, LocationUpdatesBroadcastReceiver::class.java)
        intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        PendingIntent.getBroadcast(this@MainActivity, 0, intent, getPendingIntentFlags(true))
    }

    private fun getPendingIntentFlags(isMutable: Boolean = false) =
        when {
            isMutable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE

            !isMutable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            else -> PendingIntent.FLAG_UPDATE_CURRENT
        }

    private val mainActivityViewModel by viewModel<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        activityListener = this@MainActivity

        permissionRequestType = PermissionRequestType.FINE_LOCATION
        //permissionRequestType = PermissionRequestType.BACKGROUND_LOCATION


        when (permissionRequestType) {
            PermissionRequestType.FINE_LOCATION -> {

                binding.apply {
                    permissionRequestButton.text =
                        getString(R.string.enable_fine_location_button_text)
                }
            }

            PermissionRequestType.BACKGROUND_LOCATION -> {

                binding.apply {
                    permissionRequestButton.text =
                        getString(R.string.enable_background_location_button_text)
                }
            }
            else -> {}
        }

        binding.permissionRequestButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "Hola $permissionRequestType", Toast.LENGTH_SHORT).show()
            when (permissionRequestType) {
                PermissionRequestType.FINE_LOCATION -> {
                    requestFineLocationPermission()
                }
                PermissionRequestType.BACKGROUND_LOCATION -> {
                    requestBackgroundLocationPermission()
                }
                else -> {}
            }
        }

        binding.startOrStopLocationUpdatesButton.apply {
            text = getString(R.string.start_receiving_location)
            setOnClickListener {
                startLocationUpdates()
                /*mainActivityViewModel.addLocation(
                    MyLocationEntity(
                        latitude = 22323.23,
                        longitude = 2122134.22,
                        foreground = isAppInForeground(this@MainActivity),
                        date = Date()
                    )
                )*/
            }
        }

    }

    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false

        appProcesses.forEach { appProcess ->
            if (appProcess.importance ==
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == context.packageName) {
                return true
            }
        }
        return false
    }

    private fun requestFineLocationPermission() {
        val permissionApproved = this@MainActivity.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ?: return

        if (permissionApproved) {
            activityListener?.displayLocationUI()
        } else {
            requestPermissionWithRationale(
                Manifest.permission.ACCESS_FINE_LOCATION,
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
                fineLocationRationalSnackbar)
        }
    }

    private fun requestBackgroundLocationPermission() {
        val permissionApproved = this@MainActivity.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ?: return

        if (permissionApproved) {
            activityListener?.displayLocationUI()
        } else {
            requestPermissionWithRationale(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE,
                backgroundRationalSnackbar)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
            REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive an empty array.
                    Log.d(TAG, "User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    activityListener?.displayLocationUI()

                else -> {

                    val permissionDeniedExplanation =
                        if (requestCode == REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) {
                            R.string.fine_permission_denied_explanation
                        } else {
                            R.string.background_permission_denied_explanation
                        }

                    Snackbar.make(
                        binding.root,
                        permissionDeniedExplanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                "com.example.broadcastreceiver",
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    override fun displayLocationUI() {

    }

    fun startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates()")

        if (!this@MainActivity.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)
        } catch (permissionRevoked: SecurityException) {
            Log.d(TAG, "Location permission revoked; details: $permissionRevoked")
            throw permissionRevoked
        }
    }

}