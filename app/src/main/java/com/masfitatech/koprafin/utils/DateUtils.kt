package com.masfitatech.koprafin.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "-"

        return try {
            val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id"))

            val date = originalFormat.parse(dateString)
            targetFormat.format(date!!)
        } catch (e: Exception) {
            "-"
        }
    }
}
