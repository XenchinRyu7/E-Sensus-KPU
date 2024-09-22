package com.saefulrdevs.esensus.viewmodel

import android.app.Application
import android.app.SharedElementCallback
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.saefulrdevs.esensus.data.model.Citizens
import com.saefulrdevs.esensus.data.repository.CitizensRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(
    application: Application,
    private val repository: CitizensRepository
) : AndroidViewModel(application) {

    val citizensList: LiveData<List<Citizens>> get() = repository.getAllCitizens()

    fun insertCitizen(citizen: Citizens, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isSuccess = repository.insertCitizen(citizen)
            withContext(Dispatchers.Main) {
                onResult(isSuccess)
            }
        }
    }

    fun updateCitizen(citizen: Citizens, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isSuccess = repository.updateCitizen(citizen)
            withContext(Dispatchers.Main) {
                onResult(isSuccess)
            }
        }
    }

    fun deleteCitizen(citizen: Citizens, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isSuccess = repository.deleteCitizen(citizen)
            withContext(Dispatchers.Main) {
                onResult(isSuccess)
            }
        }
    }

    fun searchCitizens(query: String): LiveData<List<Citizens>> {
        return repository.searchCitizens(query)
    }
}