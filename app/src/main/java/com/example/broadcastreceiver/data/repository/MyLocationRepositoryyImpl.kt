package com.example.broadcastreceiver.data.repository

import android.content.Context
import com.example.broadcastreceiver.data.model.MyLocationDao
import com.example.broadcastreceiver.data.model.MyLocationEntity

class MyLocationRepositoryyImpl(private val context: Context, private val myLocationDao: MyLocationDao): MyLocationRepository{


    override suspend fun addLocations(myLocationEntities: List<MyLocationEntity>) {
        myLocationDao.addLocations(myLocationEntities)
    }

    override suspend fun addLocation(myLocationEntity: MyLocationEntity) {
        myLocationDao.addLocation(myLocationEntity)
    }

}