package com.saefulrdevs.esensus.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.saefulrdevs.esensus.data.dao.CitizensDao
import com.saefulrdevs.esensus.data.model.Citizens
import javax.inject.Inject

class CitizensRepository @Inject constructor(private val citizensDao: CitizensDao) {
    suspend fun insertCitizen(citizens: Citizens): Boolean {
        return try {
            val rowId = citizensDao.insertCitizen(citizens)
            Log.d("CitizensRepositoryInsert", "Inserted row ID: $rowId")
            rowId != -1L
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateCitizen(citizens: Citizens): Boolean {
        return try {
            val rowsUpdated = citizensDao.updateCitizen(citizens)
            rowsUpdated > 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteCitizen(citizens: Citizens): Boolean {
        return try {
            val rowsDeleted = citizensDao.deleteCitizen(citizens)
            rowsDeleted > 0
        } catch (e: Exception) {
            false
        }
    }

    fun getAllCitizens(): LiveData<List<Citizens>> {
        return citizensDao.getAllCitizens()
    }

    suspend fun getCitizenById(nik: String): Citizens {
        return citizensDao.getCitizenById(nik)
    }

}