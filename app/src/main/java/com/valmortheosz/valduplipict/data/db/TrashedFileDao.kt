package com.valmortheosz.valduplipict.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.valmortheosz.valduplipict.data.model.TrashedFile
import kotlinx.coroutines.flow.Flow

@Dao
interface TrashedFileDao {
    @Query("SELECT * FROM trashed_files ORDER BY deletedDate DESC")
    fun getAllTrashedFiles(): Flow<List<TrashedFile>>

    @Query("SELECT * FROM trashed_files ORDER BY deletedDate DESC")
    suspend fun getAllTrashedFilesList(): List<TrashedFile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrashedFile(file: TrashedFile)

    @Delete
    suspend fun deleteTrashedFile(file: TrashedFile)

    @Query("DELETE FROM trashed_files")
    suspend fun clearAll()
}
