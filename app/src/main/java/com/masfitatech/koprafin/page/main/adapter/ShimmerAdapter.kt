package com.masfitatech.koprafin.page.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.masfitatech.koprafin.R

class ShimmerAdapter(private val itemCount: Int = 10) :
    RecyclerView.Adapter<ShimmerAdapter.ShimmerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shimmer_product_card, parent, false)
        return ShimmerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShimmerViewHolder, position: Int) {
        // Tidak ada binding data karena ini hanya placeholder shimmer
    }

    override fun getItemCount(): Int = itemCount

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
