package com.example.broadcastreceiver.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.broadcastreceiver.data.model.MyLocationDao
import com.example.broadcastreceiver.data.model.MyLocationEntity

@Database(entities = [MyLocationEntity::class], version = 1, exportSchema = false)
@TypeConverters(MyLocationTypeConverters::class)
abstract class MyLocationDatabase: RoomDatabase() {

    abstract fun myLocationDao(): MyLocationDao

}