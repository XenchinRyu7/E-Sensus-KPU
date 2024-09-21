package com.saefulrdevs.esensus.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saefulrdevs.esensus.data.model.Citizens

@Dao
interface CitizensDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCitizen(citizens: Citizens): Long

    @Update
    suspend fun updateCitizen(citizens: Citizens): Int

    @Delete
    suspend fun deleteCitizen(citizens: Citizens): Int

    @Query("SELECT * FROM citizens")
    fun getAllCitizens(): LiveData<List<Citizens>>

    @Query("SELECT * FROM citizens WHERE nik = :nik")
    suspend fun getCitizenById(nik: String): Citizens
}
