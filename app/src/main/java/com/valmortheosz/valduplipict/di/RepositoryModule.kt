package com.valmortheosz.valduplipict.di

import android.content.Context
import com.valmortheosz.valduplipict.data.db.ImageDao
import com.valmortheosz.valduplipict.data.repository.DuplicateRepository
import com.valmortheosz.valduplipict.data.repository.ScanRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideScanRepository(
        @ApplicationContext context: Context,
        imageDao: ImageDao
    ): ScanRepository {
        return ScanRepository(context, imageDao)
    }

    @Provides
    @Singleton
    fun provideDuplicateRepository(): DuplicateRepository {
        return DuplicateRepository()
    }
}
