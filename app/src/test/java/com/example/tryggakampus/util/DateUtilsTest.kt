package com.example.tryggakampus.util

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class DateUtilsTest {

    @Test
    fun addDays_and_isSameDay_work() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val d1 = cal.time
        val d2 = DateUtils.addDays(d1, 1)
        assertFalse(DateUtils.isSameDay(d1, d2))
        val d3 = DateUtils.addDays(d2, -1)
        assertTrue(DateUtils.isSameDay(d1, d3))
    }

    @Test
    fun formatDate_and_weekOfYear() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 31)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val date = cal.time
        val str = DateUtils.formatDate(date, pattern = "yyyy-MM-dd")
        assertEquals("2024-12-31", str)
        val week = DateUtils.getWeekOfYear(date)
        assertTrue(week in 1..53)
    }

    @Test
    fun startEndOfDay_bounds() {
        val date = Date()
        val start = DateUtils.getStartOfDay(date)
        val end = DateUtils.getEndOfDay(date)
        val fmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        assertEquals("00:00:00.000", fmt.format(start))
        assertEquals("23:59:59.999", fmt.format(end))
        assertTrue(start.before(end))
    }
}

