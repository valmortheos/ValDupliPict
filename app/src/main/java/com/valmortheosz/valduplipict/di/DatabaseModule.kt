package com.valmortheosz.valduplipict.di

import android.content.Context
import androidx.room.Room
import com.valmortheosz.valduplipict.data.db.AppDatabase
import com.valmortheosz.valduplipict.data.db.ImageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "valduplipict_database"
        ).fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideImageDao(database: AppDatabase): ImageDao {
        return database.imageDao()
    }
}
