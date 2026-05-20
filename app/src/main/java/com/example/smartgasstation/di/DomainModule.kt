package com.example.smartgasstation.di

import com.example.smartgasstation.domain.repository.IGasStationRepository
import com.example.smartgasstation.domain.repository.IRefuelRepository
import com.example.smartgasstation.domain.usecase.AddRefuelUseCase
import com.example.smartgasstation.domain.usecase.ClearHistoryUseCase
import com.example.smartgasstation.domain.usecase.DeleteRefuelUseCase
import com.example.smartgasstation.domain.usecase.ExportToPdfUseCase
import com.example.smartgasstation.domain.usecase.ExportToTxtUseCase
import com.example.smartgasstation.domain.usecase.ExportToXlsUseCase
import com.example.smartgasstation.domain.usecase.FindBestStationUseCase
import com.example.smartgasstation.domain.usecase.GetRefuelRecordsUseCase
import com.example.smartgasstation.domain.usecase.ImportFromTxtUseCase
import com.example.smartgasstation.domain.usecase.ImportFromXlsUseCase
import com.example.smartgasstation.domain.usecase.UpdateRefuelUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object DomainModule {
    @Provides
    fun provideAddRefuelUseCase(repository: IRefuelRepository): AddRefuelUseCase {
        return AddRefuelUseCase(repository)
    }

    @Provides
    fun provideClearHistoryUseCase(repository: IRefuelRepository): ClearHistoryUseCase {
        return ClearHistoryUseCase(repository)
    }

    @Provides
    fun provideDeleteRefuelUseCase(repository: IRefuelRepository): DeleteRefuelUseCase {
        return DeleteRefuelUseCase(repository)
    }

    @Provides
    fun provideExportToPdfUseCase(repository: IRefuelRepository): ExportToPdfUseCase {
        return ExportToPdfUseCase(repository)
    }

    @Provides
    fun provideExportToTxtUseCase(repository: IRefuelRepository): ExportToTxtUseCase {
        return ExportToTxtUseCase(repository)
    }

    @Provides
    fun provideExportToXlsUseCase(repository: IRefuelRepository): ExportToXlsUseCase {
        return ExportToXlsUseCase(repository)
    }

    @Provides
    fun provideFindBestStationUseCase(repository: IGasStationRepository): FindBestStationUseCase {
        return FindBestStationUseCase(repository)
    }

    @Provides
    fun provideGetRefuelRecordsUseCase(repository: IRefuelRepository): GetRefuelRecordsUseCase {
        return GetRefuelRecordsUseCase(repository)
    }

    @Provides
    fun provideImportFromTxtUseCase(repository: IRefuelRepository): ImportFromTxtUseCase {
        return ImportFromTxtUseCase(repository)
    }

    @Provides
    fun provideImportFromXlsUseCase(repository: IRefuelRepository): ImportFromXlsUseCase {
        return ImportFromXlsUseCase(repository)
    }

    @Provides
    fun provideUpdateRefuelUseCase(repository: IRefuelRepository): UpdateRefuelUseCase {
        return UpdateRefuelUseCase(repository)
    }
}