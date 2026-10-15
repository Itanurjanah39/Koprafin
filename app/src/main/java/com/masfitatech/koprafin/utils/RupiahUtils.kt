package com.masfitatech.koprafin.utils

import java.text.NumberFormat
import java.util.Locale

object RupiahUtils {

    fun toRupiah(value: String?): String {
        if (value.isNullOrEmpty()) return "Rp 0"

        // Hapus koma atau titik desimal jika ada
        val cleanValue = value.replace(".00", "").replace(",", "")

        return try {
            val number = cleanValue.toDouble()
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            format.format(number)
                .replace(",00", "")  // hilangkan desimal
        } catch (e: Exception) {
            "Rp 0"
        }
    }

    fun toRupiah(number: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(number)
            .replace(",00", "")
    }
}
