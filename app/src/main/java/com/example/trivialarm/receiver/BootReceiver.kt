package com.example.trivialarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.trivialarm.data.local.dao.AlarmDao
import com.example.trivialarm.data.repository.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val alarms = alarmRepository.getAllAlarms().first()
                alarms.forEach { alarm ->
                    if (alarm.isEnabled) {
                        alarmRepository.scheduleAlarm(alarm)
                    }
                }
            }
        }
    }
}
