package com.saefulrdevs.esensus.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.saefulrdevs.esensus.data.dao.CitizensDao
import com.saefulrdevs.esensus.data.model.Citizens

@Database(entities = [Citizens::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun citizenDao(): CitizensDao
}