package com.saefulrdevs.esensus.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.saefulrdevs.esensus.data.dao.CitizensDao

class Database private constructor(context: Context) {

    private val appDatabase: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "esensus.db"
    ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
        .fallbackToDestructiveMigration()
        .build()

    companion object {
        @Volatile
        private var INSTANCE: Database? = null

        fun getInstance(context: Context): Database {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Database(context).also { INSTANCE = it }
            }
        }
    }

    fun CitizensDao(): CitizensDao = appDatabase.citizenDao()
}

