package com.example.smartgasstation.di

import com.example.smartgasstation.data.RefuelDao
import com.example.smartgasstation.data.RefuelRepository
import com.example.smartgasstation.data.api.GasStationApi
import com.example.smartgasstation.data.repository.GasStationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRefuelRepository(dao: RefuelDao): RefuelRepository {
        return RefuelRepository(dao)
    }

    @Provides
    @Singleton
    fun provideGasStationRepository(api: GasStationApi): GasStationRepository {
        return GasStationRepository(api)
    }
}
