package com.example.trivialarm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.trivialarm.R
import com.example.trivialarm.data.local.entity.AlarmEntity
import com.example.trivialarm.ui.viewmodel.AlarmViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlarmScreen(
    alarmId: Int?,
    viewModel: AlarmViewModel,
    onBack: () -> Unit
) {
    val calendar = Calendar.getInstance()
    var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            TimePicker(state = timePickerState)

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

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.saveAlarm(
                        timePickerState.hour,
                        timePickerState.minute,
                        selectedDays.toList().sorted(),
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
