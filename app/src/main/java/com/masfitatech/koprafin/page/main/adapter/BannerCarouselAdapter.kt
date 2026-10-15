package com.masfitatech.koprafin.page.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.masfitatech.koprafin.data.model.DataBanner
import com.masfitatech.koprafin.data.network.ApiClient
import com.masfitatech.koprafin.databinding.ItemBannerCarouselBinding

class BannerCarouselAdapter(
    private var list: List<DataBanner>
) : RecyclerView.Adapter<BannerCarouselAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemBannerCarouselBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBannerCarouselBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val banner = list[position]

        Glide.with(holder.itemView.context)
            .load(ApiClient.BASE_URL_IMAGE+ (banner.image))
            .into(holder.binding.imgBanner)
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<DataBanner>) {
        list = newList
        notifyDataSetChanged()
    }
}
