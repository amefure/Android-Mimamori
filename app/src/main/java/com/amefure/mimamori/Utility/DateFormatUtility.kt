package com.amefure.mimamori.Utility

import java.text.SimpleDateFormat
import java.util.Date

class DateFormatUtility(format: String = "yyyy年MM月dd日") {

    private val df = SimpleDateFormat(format)

    public fun getString(date: Date): String {
        return df.format(date)
    }

}