package com.saefulrdevs.esensus.di

import android.content.Context
import com.saefulrdevs.esensus.data.dao.CitizensDao
import com.saefulrdevs.esensus.data.database.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return Database.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideCitizensDao(database: Database): CitizensDao {
        return database.CitizensDao()
    }
}