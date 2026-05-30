package com.justprotein.data

import kotlinx.coroutines.flow.Flow

class ProteinRepository(private val proteinDao: ProteinDao) {
    val allEntries: Flow<List<ProteinEntry>> = proteinDao.getAllEntries()

    suspend fun insert(entry: ProteinEntry) {
        proteinDao.insert(entry)
    }

    suspend fun delete(entry: ProteinEntry) {
        proteinDao.delete(entry)
    }

    suspend fun undoLastEntry() {
        proteinDao.deleteLastEntry()
    }

    suspend fun deleteAll() {
        proteinDao.deleteAll()
    }
}
