package com.masfitatech.koprafin.page.main.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.model.DataProduct
import com.masfitatech.koprafin.data.network.ApiClient
import com.masfitatech.koprafin.databinding.ItemProductHomeBinding
import com.masfitatech.koprafin.utils.RupiahUtils

/** Kartu produk ringkas khusus beranda (gambar kiri, info kanan). */
class HomeProductAdapter(
    private val onClick: (DataProduct) -> Unit
) : RecyclerView.Adapter<HomeProductAdapter.ViewHolder>() {

    private val items = mutableListOf<DataProduct>()

    inner class ViewHolder(val binding: ItemProductHomeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemProductHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvName.text = item.name
        holder.binding.tvPrice.text = RupiahUtils.toRupiah(item.price)
        holder.binding.tvCategory.text = item.category?.name ?: "Kategori"

        Glide.with(holder.itemView.context)
            .load(ApiClient.BASE_URL_IMAGE + (item.picture?.firstOrNull() ?: ""))
            .placeholder(R.drawable.empty_product)
            .into(holder.binding.imgProduct)

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<DataProduct>) {
        items.clear()
        items.addAll(newData)
        notifyDataSetChanged()
    }
}
