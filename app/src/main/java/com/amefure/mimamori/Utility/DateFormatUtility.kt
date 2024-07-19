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

        /** 同じ日付かどうか */
        public fun isSameDate(date1: Date, date2: Date): Boolean {
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.time = date1
            cal2.time = date2

            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
        }
    }

}