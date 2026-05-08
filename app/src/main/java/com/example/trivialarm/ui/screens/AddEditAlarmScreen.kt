package com.example.trivialarm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trivialarm.R
import com.example.trivialarm.data.local.entity.AlarmDifficultyPreset
import com.example.trivialarm.ui.viewmodel.AlarmViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlarmScreen(
    alarmId: Int?,
    viewModel: AlarmViewModel,
    onBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    
    val calendar = Calendar.getInstance()
    var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedDifficulty by remember { mutableStateOf(AlarmDifficultyPreset.MEDIUM) }

    val timePickerState = rememberTimePickerState(
        initialHour = selectedHour,
        initialMinute = selectedMinute,
        is24Hour = true
    )

    LaunchedEffect(alarmId) {
        if (alarmId != null) {
            val alarm = viewModel.getAlarmById(alarmId)
            if (alarm != null) {
                selectedHour = alarm.hour
                selectedMinute = alarm.minute
                selectedDays = alarm.daysOfWeek.toSet()
                selectedCategoryId = alarm.categoryId
                selectedDifficulty = alarm.difficultyPreset
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (alarmId == null) stringResource(R.string.add_alarm) else stringResource(R.string.edit_alarm)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            TimePicker(state = timePickerState)

            // Repeat Days
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.repeat),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val days = listOf(1, 2, 3, 4, 5, 6, 7)
                    items(days) { day ->
                        FilterChip(
                            selected = selectedDays.contains(day),
                            onClick = {
                                selectedDays = if (selectedDays.contains(day)) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            },
                            label = { Text(dayToString(day)) }
                        )
                    }
                }
            }

            // Category Dropdown
            var categoryExpanded by remember { mutableStateOf(false) }
            val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: "Random"
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategoryName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Random") },
                            onClick = {
                                selectedCategoryId = null
                                categoryExpanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Difficulty Selection
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Challenge Difficulty",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AlarmDifficultyPreset.entries.forEachIndexed { index, preset ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = AlarmDifficultyPreset.entries.size),
                            onClick = { selectedDifficulty = preset },
                            selected = selectedDifficulty == preset
                        ) {
                            Text("${preset.name.lowercase().replaceFirstChar { it.uppercase() }} (${preset.questionCount})")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.saveAlarm(
                        timePickerState.hour,
                        timePickerState.minute,
                        selectedDays.toList().sorted(),
                        selectedCategoryId,
                        selectedDifficulty,
                        alarmId
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_alarm))
            }
        }
    }
}
