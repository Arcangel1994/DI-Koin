package com.example.broadcastreceiver.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.broadcastreceiver.MainActivityViewModel
import com.example.broadcastreceiver.data.MyLocationDatabase
import com.example.broadcastreceiver.data.model.MyLocationDao
import com.example.broadcastreceiver.data.repository.MyLocationRepository
import com.example.broadcastreceiver.data.repository.MyLocationRepositoryyImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {

    fun provideDatabase(application: Application): MyLocationDatabase {
        return Room.databaseBuilder(application, MyLocationDatabase::class.java, "my_location_database")
            .build()
    }

    fun provideDao(database: MyLocationDatabase): MyLocationDao {
        return  database.myLocationDao()
    }

    single { provideDatabase(androidApplication()) }
    single { provideDao(get()) }
}

val repositoryModule = module {

    fun myLocationRepository(context: Context, dao : MyLocationDao): MyLocationRepository {
        return MyLocationRepositoryyImpl(context, dao)
    }
    single { myLocationRepository(androidContext(), get()) }

}

val viewModelModule = module {

    // Specific viewModel pattern to tell Koin how to build CountriesViewModel
    viewModel {
        MainActivityViewModel(repository = get())
    }

}

val applicationModule = listOf(
    databaseModule,
    repositoryModule,
    viewModelModule
)