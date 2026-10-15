package com.masfitatech.koprafin.page.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.model.DataCategory
import com.masfitatech.koprafin.databinding.ItemCategoryBinding

class CategoryAdapter(
    private var list: List<DataCategory>,
    private val onEdit: (DataCategory) -> Unit,
    private val onDelete: (DataCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.tvNameCategory.text = item.name
        holder.binding.tvTenor.text = "${item.minTenor} - ${item.maksTenor} Bulan"
        holder.binding.tvMargin.text = item.margin.toString()

        holder.binding.root.setOnClickListener {
            onEdit(item)
        }

        holder.binding.ivOption.setOnClickListener {
            showMenu(it, item)
        }
    }

    private fun showMenu(view: View, item: DataCategory) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.menu_edit_delete, popup.menu)

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_edit -> {
                    onEdit(item)
                    true
                }
                R.id.menu_delete -> {
                    onDelete(item)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<DataCategory>) {
        list = newList
        notifyDataSetChanged()
    }
}
