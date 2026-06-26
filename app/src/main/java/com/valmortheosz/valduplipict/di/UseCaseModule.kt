package com.valmortheosz.valduplipict.di

import android.content.Context
import com.valmortheosz.valduplipict.data.repository.ScanRepository
import com.valmortheosz.valduplipict.domain.algorithm.HashEngine
import com.valmortheosz.valduplipict.domain.algorithm.HistogramComparator
import com.valmortheosz.valduplipict.domain.algorithm.SSIMCalculator
import com.valmortheosz.valduplipict.domain.usecase.DeleteDuplicatesUseCase
import com.valmortheosz.valduplipict.domain.usecase.ScanImagesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideScanImagesUseCase(
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context,
        scanRepository: ScanRepository,
        hashEngine: HashEngine,
        ssimCalculator: SSIMCalculator,
        histogramComparator: HistogramComparator
    ): ScanImagesUseCase {
        return ScanImagesUseCase(context, scanRepository, hashEngine, ssimCalculator, histogramComparator)
    }

    @Provides
    @Singleton
    fun provideDeleteDuplicatesUseCase(
        @ApplicationContext context: Context,
        trashedFileDao: com.valmortheosz.valduplipict.data.db.TrashedFileDao
    ): DeleteDuplicatesUseCase {
        return DeleteDuplicatesUseCase(context, trashedFileDao)
    }

    @Provides
    @Singleton
    fun provideHashEngine(): HashEngine {
        return HashEngine()
    }

    @Provides
    @Singleton
    fun provideSSIMCalculator(): SSIMCalculator {
        return SSIMCalculator()
    }

    @Provides
    @Singleton
    fun provideHistogramComparator(): HistogramComparator {
        return HistogramComparator()
    }
}
