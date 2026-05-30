package com.justprotein.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.justprotein.data.ProteinEntry
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(viewModel: ProteinViewModel) {
    val entries by viewModel.allEntries.collectAsState()
    val goal by viewModel.dailyGoal.collectAsState()

    val historyItems = rememberHistoryItems(entries, goal)
    val average = if (historyItems.isNotEmpty()) historyItems.map { it.total }.average().toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "History",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("7-Day Average", style = MaterialTheme.typography.titleMedium)
                Text("${average}g", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(historyItems) { item ->
                HistoryItemRow(item, goal)
            }
        }
    }
}

data class HistoryDay(
    val date: String,
    val total: Int,
    val goalReached: Boolean
)

@Composable
fun rememberHistoryItems(entries: List<ProteinEntry>, goal: Int): List<HistoryDay> {
    val dateFormat = SimpleDateFormat("MMM dd, EEE", Locale.getDefault())
    val calendar = Calendar.getInstance()
    val history = mutableListOf<HistoryDay>()

    for (i in 0 until 7) {
        val dateStart = calendar.clone() as Calendar
        dateStart.set(Calendar.HOUR_OF_DAY, 0)
        dateStart.set(Calendar.MINUTE, 0)
        dateStart.set(Calendar.SECOND, 0)
        dateStart.set(Calendar.MILLISECOND, 0)
        
        val nextDay = dateStart.clone() as Calendar
        nextDay.add(Calendar.DAY_OF_YEAR, 1)

        val dayTotal = entries.filter {
            it.timestamp >= dateStart.timeInMillis && it.timestamp < nextDay.timeInMillis
        }.sumOf { it.grams }

        history.add(
            HistoryDay(
                date = if (i == 0) "Today" else dateFormat.format(dateStart.time),
                total = dayTotal,
                goalReached = dayTotal >= goal
            )
        )
        calendar.add(Calendar.DAY_OF_YEAR, -1)
    }
    return history
}

@Composable
fun HistoryItemRow(item: HistoryDay, goal: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(item.date, style = MaterialTheme.typography.titleMedium)
                Text("${item.total} / ${goal}g", style = MaterialTheme.typography.bodyMedium)
            }
            if (item.goalReached) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Goal reached", tint = Color(0xFF4CAF50))
            } else {
                Icon(Icons.Default.RadioButtonUnchecked, contentDescription = "Goal not reached", tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
