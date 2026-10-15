package com.masfitatech.koprafin.page.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.model.DataBanner
import com.masfitatech.koprafin.data.network.ApiClient
import com.masfitatech.koprafin.databinding.ItemImageBannerAddBinding

class BannerAdapter(
    private var list: List<DataBanner>,
    private val onToggle: (DataBanner, Boolean) -> Unit,
    private val onDelete: (DataBanner) -> Unit
) : RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemImageBannerAddBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageBannerAddBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val banner = list[position]

        // ====== PENTING ======
        holder.binding.cvAddImage.visibility = View.GONE
        holder.binding.btnUpload.visibility = View.GONE
        // =====================

        holder.binding.switchStatus.isChecked = banner.status == 1

        Glide.with(holder.itemView.context)
            .load(ApiClient.BASE_URL_IMAGE +( banner.image))
            .into(holder.binding.imgBanner)

        holder.binding.switchStatus.setOnCheckedChangeListener { _, isChecked ->
            onToggle(banner, isChecked)
        }

        holder.binding.root.setOnLongClickListener {
            onDelete(banner)
            true
        }
    }


    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<DataBanner>) {
        list = newList
        notifyDataSetChanged()
    }
}
