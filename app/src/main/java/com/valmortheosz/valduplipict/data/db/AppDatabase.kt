package com.valmortheosz.valduplipict.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.valmortheosz.valduplipict.data.model.ImageFile

@Database(entities = [ImageFile::class, com.valmortheosz.valduplipict.data.model.TrashedFile::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
    abstract fun trashedFileDao(): TrashedFileDao
}
