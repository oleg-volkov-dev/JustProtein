package com.justprotein.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protein_entries")
data class ProteinEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val grams: Int,
    val timestamp: Long = System.currentTimeMillis()
)
