package com.masfitatech.koprafin.utils

import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.masfitatech.koprafin.R

/** Gaya badge status kredit: latar lembut + teks berwarna. */
object StatusStyle {

    data class Colors(@ColorRes val text: Int, @ColorRes val background: Int)

    fun colorsFor(status: String?): Colors = when (status?.lowercase()) {
        "pengajuan" -> Colors(R.color.statusPengajuan, R.color.statusPengajuanBg)
        "berjalan"  -> Colors(R.color.statusBerjalan, R.color.statusBerjalanBg)
        "selesai"   -> Colors(R.color.statusSelesai, R.color.statusSelesaiBg)
        "ditolak"   -> Colors(R.color.statusDitolak, R.color.statusDitolakBg)
        else        -> Colors(R.color.statusDefault, R.color.statusDefaultBg)
    }

    fun apply(view: TextView, status: String?) {
        val st = status?.lowercase().orEmpty()
        val c = colorsFor(st)
        val ctx = view.context
        view.text = st.replaceFirstChar { it.uppercase() }.ifEmpty { "-" }
        view.setTextColor(ContextCompat.getColor(ctx, c.text))
        view.backgroundTintList = ContextCompat.getColorStateList(ctx, c.background)
    }
}
