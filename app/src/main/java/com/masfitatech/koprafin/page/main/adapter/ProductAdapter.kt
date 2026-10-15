package com.masfitatech.koprafin.page.main.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.model.DataProduct
import com.masfitatech.koprafin.data.network.ApiClient
import com.masfitatech.koprafin.databinding.ItemProductCardBinding
import com.masfitatech.koprafin.utils.RupiahUtils

class ProductAdapter(
    private val items: MutableList<DataProduct>,
    private val onClick: (DataProduct) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private val fullList = mutableListOf<DataProduct>()

    val currentList: List<DataProduct>
        get() = items


    inner class ViewHolder(val binding: ItemProductCardBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

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

    // Update dari API
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<DataProduct>) {
        items.clear()
        items.addAll(newData)

        // Refresh fullList agar filter tetap valid
        fullList.clear()
        fullList.addAll(newData)

        notifyDataSetChanged()
    }

    // 🔍 FILTERING LOGIC
    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        val search = query.lowercase()

        items.clear()

        if (search.isBlank()) {
            // kosongkan search → tampilkan semua produk
            items.addAll(fullList)
        } else {
            // filter memakai name + category
            val filtered = fullList.filter { product ->
                product.name?.lowercase()?.contains(search) == true ||
                        product.category?.name?.lowercase()?.contains(search) == true
            }
            items.addAll(filtered)
        }

        notifyDataSetChanged()
    }
}


//class ProductAdapter(
//    private val items: MutableList<DataProduct>,
//    private val onClick: (DataProduct) -> Unit
//) : RecyclerView.Adapter<ProductAdapter.ViewHolder>(){
//
//    inner class ViewHolder(val binding: ItemProductCardBinding)
//        : RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        viewType: Int
//    ): ViewHolder {
//        val binding = ItemProductCardBinding.inflate(
//            LayoutInflater.from(parent.context), parent, false
//        )
//        return ViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val item = items[position]
//
//        holder.binding.tvName.text = item.name
//        holder.binding.tvPrice.text = RupiahUtils.toRupiah(item.price)
//        holder.binding.tvCategory.text = item.category?.name ?: "Kategori"
//
//        Glide.with(holder.itemView.context)
//            .load(ApiClient.BASE_URL_IMAGE + (item.picture?.firstOrNull() ?: ""))
//            .placeholder(R.drawable.empty_product)
//            .into(holder.binding.imgProduct)
//
//        holder.itemView.setOnClickListener {
//            onClick(item)
//        }
//
//
//    }
//
//    override fun getItemCount(): Int = items.size
//    @SuppressLint("NotifyDataSetChanged")
//    fun updateData(newData: List<DataProduct>) {
//        items.clear()
//        items.addAll(newData)
//        notifyDataSetChanged()
//    }
//}