package com.example.trivialarm

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.trivialarm.service.AlarmService
import com.example.trivialarm.ui.navigation.NavGraph
import com.example.trivialarm.ui.navigation.Route
import com.example.trivialarm.ui.theme.TriviAlarmTheme
import com.example.trivialarm.ui.viewmodel.AlarmViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var currentIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        currentIntent = intent
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        enableEdgeToEdge()
        setContent {
            TriviAlarmTheme {
                val context = LocalContext.current
                val viewModel: AlarmViewModel = hiltViewModel()
                val backStack = rememberNavBackStack(Route.AlarmList)

                // Permission handling
                val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
                
                var hasNotificationPermission by remember {
                    mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        } else true
                    )
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasNotificationPermission = isGranted
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (!alarmManager.canScheduleExactAlarms()) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.fromParts("package", packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    }
                }

                LaunchedEffect(currentIntent) {
                    val intent = currentIntent
                    if (intent?.getBooleanExtra("ALARM_TRIGGERED", false) == true) {
                        val alarmId = intent.getIntExtra("ALARM_ID", -1)
                        if (backStack.lastOrNull() !is Route.AlarmActive) {
                            backStack.add(Route.AlarmActive(alarmId))
                        }
                    }
                }
                
                NavGraph(
                    backStack = backStack as NavBackStack<Route>,
                    onBack = { 
                        if (backStack.size > 1 && backStack.lastOrNull() !is Route.AlarmActive) {
                            backStack.removeAt(backStack.size - 1)
                        }
                    },
                    viewModel = viewModel,
                    onStopAlarm = {
                        stopService(Intent(this, AlarmService::class.java))
                        backStack.clear()
                        backStack.add(Route.AlarmList)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent = intent
    }
}
