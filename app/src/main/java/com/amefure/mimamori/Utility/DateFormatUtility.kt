package com.amefure.mimamori.Utility

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class DateFormatUtility(format: String = "yyyy年M月d日") {

    private val df = SimpleDateFormat(format)

    public fun getString(date: Date): String {
        return df.format(date)
    }
    companion object {
        /** Date型を[23:59:99]にして返却 */
        public fun resetEndTime(date: Date): Date {
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 99)

            return calendar.time
        }
    }

}