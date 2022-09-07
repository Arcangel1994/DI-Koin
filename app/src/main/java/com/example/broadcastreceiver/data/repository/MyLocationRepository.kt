package com.example.broadcastreceiver.data.repository

import android.content.Context
import com.example.broadcastreceiver.data.model.MyLocationDao
import com.example.broadcastreceiver.data.model.MyLocationEntity

interface MyLocationRepository {

    suspend fun addLocations (myLocationEntities: List<MyLocationEntity>)

    suspend fun addLocation (myLocationEntity: MyLocationEntity)

}