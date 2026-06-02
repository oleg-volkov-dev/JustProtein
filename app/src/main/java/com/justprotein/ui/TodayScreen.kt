package com.justprotein.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.LayoutDirection
import com.justprotein.R
import com.justprotein.ui.theme.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justprotein.data.ProteinEntry
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TodayScreen(viewModel: ProteinViewModel) {
    val entries by viewModel.allEntries.collectAsState()
    val goal by viewModel.dailyGoal.collectAsState()
    
    val todayEntries = entries.filter { viewModel.isToday(it.timestamp) }
    val currentTotal = todayEntries.sumOf { it.grams }
    val progressRatio = if (goal > 0) (currentTotal.toFloat() / goal) else 0f
    val progress = progressRatio.coerceAtMost(1f)
    
    val targetColor = when {
        progressRatio < 0.2f -> ProgressRed
        progressRatio < 0.5f -> ProgressDarkOrange
        progressRatio < 0.7f -> ProgressLightOrange
        progressRatio < 1.0f -> ProgressGreen
        else -> Gold
    }
    
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500)
    )

    val infiniteTransition = rememberInfiniteTransition()
    val goldOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    var showCustomDialog by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Today", style = MaterialTheme.typography.headlineMedium)
        
        ProteinBullMascot(
            progress = progress,
            modifier = Modifier.size(220.dp)
        )
        
        Text(
            text = "$currentTotal / ${goal}g",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (progressRatio >= 1.0f) {
            // Shiny Gold Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Gold, GoldLight, Gold),
                            start = androidx.compose.ui.geometry.Offset(goldOffset, 0f),
                            end = androidx.compose.ui.geometry.Offset(goldOffset + 300f, 0f),
                            tileMode = androidx.compose.ui.graphics.TileMode.Mirror
                        )
                    )
            )
        } else {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = animatedColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickAddButton(10) { viewModel.addProtein(10) }
            QuickAddButton(20) { viewModel.addProtein(20) }
            QuickAddButton(30) { viewModel.addProtein(30) }
            QuickAddButton(40) { viewModel.addProtein(40) }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { showCustomDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Custom")
            }
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedButton(onClick = { viewModel.undoLastEntry() }) {
                Icon(Icons.Default.Undo, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Undo")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Today's Entries",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Start)
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(todayEntries) { entry ->
                EntryItem(entry) { viewModel.deleteEntry(entry) }
            }
        }
    }

    if (showCustomDialog) {
        CustomAddDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { grams ->
                viewModel.addProtein(grams)
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun QuickAddButton(grams: Int, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(70.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text("+$grams", fontSize = 16.sp)
    }
}

@Composable
fun EntryItem(entry: ProteinEntry, onDelete: () -> Unit) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = timeFormat.format(Date(entry.timestamp))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("${entry.grams}g", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(timeString, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete entry")
            }
        }
    }
}

@Composable
fun CustomAddDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var text by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Protein") },
        text = {
            TextField(
                value = text,
                onValueChange = { if (it.all { char -> char.isDigit() }) text = it },
                label = { Text("Grams") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    text.toIntOrNull()?.let { onConfirm(it) }
                },
                enabled = text.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ProteinBullMascotPreview() {
    ProteinBullMascot(progress = 0.5f, modifier = Modifier.size(220.dp))
}

@Composable
fun ProteinBullMascot(progress: Float, modifier: Modifier = Modifier) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(durationMillis = 1000)
    )

    val facePainter = when {
        clampedProgress < 0.3f -> painterResource(id = R.drawable.sad)
        clampedProgress < 0.6f -> painterResource(id = R.drawable.neutral)
        else -> painterResource(id = R.drawable.happy)
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // 1. Frame
        Image(
            painter = painterResource(id = R.drawable.frame),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // 2. Clipped full_fill
        Image(
            painter = painterResource(id = R.drawable.full_fill),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(VerticalProgressShape(animatedProgress)),
            contentScale = ContentScale.Fit
        )

        // 3. Parts fill
        Image(
            painter = painterResource(id = R.drawable.parts_fill),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // 4. Face
        Image(
            painter = facePainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

class VerticalProgressShape(private val progress: Float) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): Outline {
        val path = Path().apply {
            val top = size.height * (1f - progress)
            addRect(androidx.compose.ui.geometry.Rect(0f, top, size.width, size.height))
        }
        return Outline.Generic(path)
    }
}
