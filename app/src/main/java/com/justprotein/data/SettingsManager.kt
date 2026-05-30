package com.justprotein.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val DAILY_GOAL_KEY = intPreferencesKey("daily_goal_grams")
    }

    val dailyGoal: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[DAILY_GOAL_KEY] ?: 160
        }

    suspend fun setDailyGoal(grams: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_GOAL_KEY] = grams
        }
    }
}
