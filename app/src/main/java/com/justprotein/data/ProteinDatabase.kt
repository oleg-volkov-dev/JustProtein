package com.justprotein.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProteinEntry::class], version = 1, exportSchema = false)
abstract class ProteinDatabase : RoomDatabase() {
    abstract fun proteinDao(): ProteinDao

    companion object {
        @Volatile
        private var INSTANCE: ProteinDatabase? = null

        fun getDatabase(context: Context): ProteinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProteinDatabase::class.java,
                    "protein_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
