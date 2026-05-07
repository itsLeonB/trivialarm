package com.example.trivialarm.util

import java.util.Calendar

object AlarmUtils {
    fun getNextTriggerTime(hour: Int, minute: Int, daysOfWeek: List<Int>): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // If daysOfWeek is empty, schedule for the next occurrence of this time
        if (daysOfWeek.isEmpty()) {
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis
        }

        // Otherwise, find the next scheduled day
        var minTime = Long.MAX_VALUE
        val today = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...

        // Map Android Calendar days to our 1-7 (Mon-Sun)
        val androidToCustom = mapOf(
            Calendar.MONDAY to 1,
            Calendar.TUESDAY to 2,
            Calendar.WEDNESDAY to 3,
            Calendar.THURSDAY to 4,
            Calendar.FRIDAY to 5,
            Calendar.SATURDAY to 6,
            Calendar.SUNDAY to 7
        )

        val customToAndroid = androidToCustom.entries.associateBy({ it.value }, { it.key })

        for (day in daysOfWeek) {
            val scheduledCalendar = calendar.clone() as Calendar
            val targetAndroidDay = customToAndroid[day] ?: continue
            
            var daysUntil = targetAndroidDay - today
            if (daysUntil < 0 || (daysUntil == 0 && calendar.timeInMillis <= now)) {
                daysUntil += 7
            }
            
            scheduledCalendar.add(Calendar.DAY_OF_YEAR, daysUntil)
            if (scheduledCalendar.timeInMillis < minTime) {
                minTime = scheduledCalendar.timeInMillis
            }
        }

        return minTime
    }
}
