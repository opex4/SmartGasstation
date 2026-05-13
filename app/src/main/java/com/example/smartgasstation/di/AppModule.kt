package com.example.smartgasstation.di

import android.content.Context
import com.example.smartgasstation.data.AppDatabase
import com.example.smartgasstation.data.RefuelDao
import com.example.smartgasstation.filemanager.RefuelRecordsFileManager
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideRefuelDao(database: AppDatabase): RefuelDao {
        return database.refuelDao()
    }

    @Provides
    @Singleton
    fun provideRefuelRecordsFileManager(@ApplicationContext context: Context): RefuelRecordsFileManager {
        return RefuelRecordsFileManager(context)
    }
}
