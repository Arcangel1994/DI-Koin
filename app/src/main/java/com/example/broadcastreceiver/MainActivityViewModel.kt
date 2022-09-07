package com.example.broadcastreceiver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.broadcastreceiver.data.model.MyLocationEntity
import com.example.broadcastreceiver.data.repository.MyLocationRepository
import com.example.broadcastreceiver.data.repository.MyLocationRepositoryyImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivityViewModel(private val repository: MyLocationRepository): ViewModel() {

    fun addLocation(myLocationEntity: MyLocationEntity){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addLocation(myLocationEntity)
        }
    }

    fun addLocations(myLocationEntities: List<MyLocationEntity>){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addLocations(myLocationEntities)
        }
    }

}