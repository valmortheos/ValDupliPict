package com.valmortheosz.valduplipict.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.valmortheosz.valduplipict.data.model.ImageFile
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<ImageFile>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: ImageFile)

    @Query("SELECT * FROM image_files")
    suspend fun getAllImages(): List<ImageFile>

    @Query("SELECT * FROM image_files WHERE filePath = :path")
    suspend fun getImageByPath(path: String): ImageFile?

    @Query("DELETE FROM image_files WHERE filePath = :path")
    suspend fun deleteImageByPath(path: String)

    @Query("DELETE FROM image_files WHERE filePath IN (:paths)")
    suspend fun deleteImagesByPaths(paths: List<String>)

    @Query("DELETE FROM image_files")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM image_files")
    fun getTotalImageCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM image_files")
    suspend fun getTotalImageCount(): Int
}
