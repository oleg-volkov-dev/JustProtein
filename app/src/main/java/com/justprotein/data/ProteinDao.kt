package com.justprotein.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProteinDao {
    @Query("SELECT * FROM protein_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<ProteinEntry>>

    @Insert
    suspend fun insert(entry: ProteinEntry)

    @Delete
    suspend fun delete(entry: ProteinEntry)

    @Query("DELETE FROM protein_entries WHERE id = (SELECT MAX(id) FROM protein_entries)")
    suspend fun deleteLastEntry()

    @Query("DELETE FROM protein_entries")
    suspend fun deleteAll()
}
