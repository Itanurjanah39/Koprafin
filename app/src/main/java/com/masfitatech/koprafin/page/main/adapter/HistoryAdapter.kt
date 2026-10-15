package com.masfitatech.koprafin.ui.main.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.model.DataHistory
import com.masfitatech.koprafin.utils.DateUtils
import com.masfitatech.koprafin.utils.RupiahUtils

class HistoryAdapter(
    private var items: List<DataHistory>,
    private val onClick: (DataHistory) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTenor: TextView = view.findViewById(R.id.tvTenor)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<DataHistory>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]

        holder.itemView.setOnClickListener {
            onClick(item)
        }

        holder.tvProductName.text = item.product?.name ?: "-"
        holder.tvDate.text = DateUtils.formatDate(item.createdAt)
        holder.tvTenor.text = "${item.tenor ?: 0} Bulan"
        holder.tvTotal.text = RupiahUtils.toRupiah(item.total)

        val status = item.status?.lowercase() ?: ""

        holder.tvStatus.text = status.replaceFirstChar { it.uppercase() }

        val ctx = holder.itemView.context

        val colorRes = when (status) {
            "pengajuan" -> R.color.statusPengajuan
            "berjalan"  -> R.color.statusBerjalan
            "selesai"   -> R.color.colorPrimary
            "ditolak"   -> R.color.statusDitolak
            else        -> R.color.statusDefault
        }

        holder.tvStatus.backgroundTintList =
            ContextCompat.getColorStateList(ctx, colorRes)
    }

}
