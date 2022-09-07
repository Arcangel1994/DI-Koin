package com.example.broadcastreceiver

import android.app.Application
import android.content.Context
import com.example.broadcastreceiver.di.applicationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyApplication: Application(){

    companion object {
        var instance: MyApplication? = null
            private set
        val context: Context?
            get() = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        startKoin {
            androidContext(this@MyApplication)
            modules(applicationModule)
        }
    }

}