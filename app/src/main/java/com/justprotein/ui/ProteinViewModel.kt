package com.justprotein.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.justprotein.data.ProteinDatabase
import com.justprotein.data.ProteinEntry
import com.justprotein.data.ProteinRepository
import com.justprotein.data.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class ProteinViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProteinRepository
    private val settingsManager: SettingsManager

    val dailyGoal: StateFlow<Int>
    val allEntries: StateFlow<List<ProteinEntry>>

    init {
        val database = ProteinDatabase.getDatabase(application)
        repository = ProteinRepository(database.proteinDao())
        settingsManager = SettingsManager(application)
        
        dailyGoal = settingsManager.dailyGoal.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 160
        )
        
        allEntries = repository.allEntries.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
    }

    fun addProtein(grams: Int) {
        viewModelScope.launch {
            repository.insert(ProteinEntry(grams = grams))
        }
    }

    fun deleteEntry(entry: ProteinEntry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }

    fun undoLastEntry() {
        viewModelScope.launch {
            repository.undoLastEntry()
        }
    }

    fun setDailyGoal(grams: Int) {
        viewModelScope.launch {
            settingsManager.setDailyGoal(grams)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    // Helper to check if a timestamp is today
    fun isToday(timestamp: Long): Boolean {
        val today = Calendar.getInstance()
        val entryDate = Calendar.getInstance().apply { timeInMillis = timestamp }
        return today.get(Calendar.YEAR) == entryDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == entryDate.get(Calendar.DAY_OF_YEAR)
    }
}
