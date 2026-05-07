package com.example.trivialarm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import com.example.trivialarm.ui.screens.AddEditAlarmScreen
import com.example.trivialarm.ui.screens.AlarmActiveScreen
import com.example.trivialarm.ui.screens.AlarmListScreen
import com.example.trivialarm.ui.viewmodel.AlarmViewModel
import com.example.trivialarm.ui.viewmodel.TriviaViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NavGraph(
    backStack: NavBackStack<Route>,
    onBack: () -> Unit,
    viewModel: AlarmViewModel,
    onStopAlarm: () -> Unit
) {
    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        entryProvider = { route ->
            when (route) {
                is Route.AlarmList -> NavEntry(route) {
                    AlarmListScreen(
                        viewModel = viewModel,
                        onAddAlarm = { backStack.add(Route.AddEditAlarm()) },
                        onEditAlarm = { id -> backStack.add(Route.AddEditAlarm(id)) }
                    )
                }
                is Route.AddEditAlarm -> NavEntry(route) {
                    AddEditAlarmScreen(
                        alarmId = route.alarmId,
                        viewModel = viewModel,
                        onBack = onBack
                    )
                }
                is Route.AlarmActive -> NavEntry(route) {
                    val triviaViewModel: TriviaViewModel = hiltViewModel()
                    AlarmActiveScreen(
                        alarmId = route.alarmId,
                        viewModel = triviaViewModel,
                        onStopAlarm = onStopAlarm
                    )
                }
            }
        }
    )
}
