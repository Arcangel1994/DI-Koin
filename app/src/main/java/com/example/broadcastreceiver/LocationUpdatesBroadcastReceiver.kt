package com.example.broadcastreceiver

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.broadcastreceiver.data.model.MyLocationEntity
import com.example.broadcastreceiver.data.repository.MyLocationRepository
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.util.Date

private const val TAG = "MyBroadcastReceiver"

class LocationUpdatesBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    // evaluates dependency eagerly
    //val service: MyLocationRepository = get()
    // evaluates dependency lazily
    val lazyService: MyLocationRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive() context:$context, intent:$intent")

        //val myLocationRepository = EntryPointAccessors.fromApplication(context.applicationContext, DatabaseModule::class.java)
        //val provide = entryPoint.provideDatabase(context)
        //val myLocationDao = provide.myLocationDao()

        //val myLocationRepository : MyLocationDao =
        //    EntryPoints.get(context.applicationContext, DatabaseModule::class.java)
        //        .provideDatabase(context).myLocationDao()

        if (intent.action == ACTION_PROCESS_UPDATES) {

            LocationAvailability.extractLocationAvailability(intent)?.let { locationAvailability ->
                if (!locationAvailability.isLocationAvailable) {
                    Log.d(TAG, "Location services are no longer available!")
                }
            }

            LocationResult.extractResult(intent)?.let { locationResult ->
                val locations = locationResult.locations.map { location ->
                    MyLocationEntity(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        foreground = isAppInForeground(context),
                        date = Date(location.time)
                    )
                }
                if (locations.isNotEmpty()) {
                    Log.d("MyLocationEntity", locations.toString())
                    //LocationRepository.getInstance(context, Executors.newSingleThreadExecutor())
                        ///.addLocations(locations)
                    CoroutineScope(Dispatchers.IO).launch {
                        lazyService.addLocations(locations)
                    }
                    /*GlobalScope.launch(Dispatchers.IO){
                        //someModule.addLocations(locations)
                        lazyService.addLocations(locations)
                    }*/
                }
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

    companion object {
        const val ACTION_PROCESS_UPDATES =
            "com.example.broadcastreceiver.action." +
                    "PROCESS_UPDATES"
    }
}
