package com.masfitatech.koprafin.page.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.model.DataBanner
import com.masfitatech.koprafin.data.network.ApiClient
import com.masfitatech.koprafin.databinding.ItemImageBannerAddBinding
import com.masfitatech.koprafin.utils.DateUtils

/**
 * Daftar banner di Pengaturan.
 * Listener switch dilepas sebelum bind supaya item yang di-recycle
 * tidak memicu request PUT untuk banner lain (penyebab status "loncat-loncat").
 */
class BannerAdapter(
    private val onToggle: (DataBanner, Boolean) -> Unit,
    private val onDelete: (DataBanner) -> Unit
) : ListAdapter<DataBanner, BannerAdapter.ViewHolder>(Diff) {

    private var pendingIds: Set<Int> = emptySet()

    inner class ViewHolder(val binding: ItemImageBannerAddBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemImageBannerAddBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val banner = getItem(position)
        val b = holder.binding

        Glide.with(holder.itemView.context)
            .load(ApiClient.BASE_URL_IMAGE + banner.image)
            .placeholder(R.drawable.placeholder_banner)
            .error(R.drawable.placeholder_banner)
            .into(b.imgBanner)

        b.tvBannerDate.text = "Ditambahkan ${DateUtils.formatDate(banner.createdAt)}"
        bindState(holder, banner)

        b.ivDeleteBanner.setOnClickListener {
            currentItem(holder)?.let(onDelete)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_PENDING)) bindState(holder, getItem(position))
        else super.onBindViewHolder(holder, position, payloads)
    }

    private fun bindState(holder: ViewHolder, banner: DataBanner) {
        val b = holder.binding
        val ctx = holder.itemView.context
        val active = banner.status == 1
        val busy = banner.id?.let { it in pendingIds } == true

        b.switchStatus.setOnCheckedChangeListener(null)
        b.switchStatus.isChecked = active
        b.switchStatus.isEnabled = !busy
        b.ivDeleteBanner.isEnabled = !busy
        b.ivDeleteBanner.alpha = if (busy) 0.4f else 1f
        b.progressToggle.visibility = if (busy) View.VISIBLE else View.GONE

        b.tvBannerStatus.text = if (active) "● Aktif" else "● Nonaktif"
        b.tvBannerStatus.setTextColor(
            ContextCompat.getColor(ctx, if (active) R.color.statusSelesai else R.color.statusDefault)
        )

        b.switchStatus.setOnCheckedChangeListener { _, isChecked ->
            val item = currentItem(holder) ?: return@setOnCheckedChangeListener
            if (isChecked != (item.status == 1)) onToggle(item, isChecked)
        }
    }

    private fun currentItem(holder: ViewHolder): DataBanner? {
        val pos = holder.bindingAdapterPosition
        return if (pos == RecyclerView.NO_POSITION) null else getItem(pos)
    }

    fun setPendingIds(ids: Set<Int>) {
        val changed = (ids - pendingIds) + (pendingIds - ids)
        pendingIds = ids
        currentList.forEachIndexed { index, banner ->
            if (banner.id?.let { it in changed } == true) notifyItemChanged(index, PAYLOAD_PENDING)
        }
    }

    private companion object {
        const val PAYLOAD_PENDING = "pending"

        val Diff = object : DiffUtil.ItemCallback<DataBanner>() {
            override fun areItemsTheSame(oldItem: DataBanner, newItem: DataBanner) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: DataBanner, newItem: DataBanner) = oldItem == newItem
        }
    }
}
