package com.masfitatech.koprafin.page.main.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.model.DataBanner
import com.masfitatech.koprafin.data.network.ApiClient
import com.masfitatech.koprafin.databinding.ItemBannerCarouselBinding
import com.masfitatech.koprafin.databinding.ItemHeroBrandBinding

/**
 * Carousel di kartu hero beranda.
 * Posisi 0 selalu slide pengenalan brand Koprafin, sisanya banner aktif dari API.
 */
class HeroCarouselAdapter(
    private var banners: List<DataBanner> = emptyList()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_BRAND = 0
        private const val TYPE_BANNER = 1
    }

    class BrandViewHolder(binding: ItemHeroBrandBinding) :
        RecyclerView.ViewHolder(binding.root)

    class BannerViewHolder(val binding: ItemBannerCarouselBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int =
        if (position == 0) TYPE_BRAND else TYPE_BANNER

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_BRAND) {
            BrandViewHolder(ItemHeroBrandBinding.inflate(inflater, parent, false))
        } else {
            BannerViewHolder(ItemBannerCarouselBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BannerViewHolder) {
            val banner = banners[position - 1]
            Glide.with(holder.itemView.context)
                .load(ApiClient.BASE_URL_IMAGE + banner.image)
                .placeholder(R.drawable.placeholder_banner)
                .into(holder.binding.imgBanner)
        }
    }

    override fun getItemCount(): Int = 1 + banners.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<DataBanner>) {
        banners = newList
        notifyDataSetChanged()
    }
}
