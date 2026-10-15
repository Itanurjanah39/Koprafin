package com.masfitatech.koprafin.ui.employee.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.masfitatech.koprafin.R
import com.masfitatech.koprafin.data.model.DataEmployee
import com.masfitatech.koprafin.data.network.ApiClient
import com.masfitatech.koprafin.databinding.ItemEmployeeBinding
import com.masfitatech.koprafin.utils.DateUtils

class EmployeeAdapter(
    employeeList: List<DataEmployee>,
    private val onClick: (DataEmployee) -> Unit,
    private val onDelete: (DataEmployee) -> Unit
) : RecyclerView.Adapter<EmployeeAdapter.ViewHolder>() {
    private val list = employeeList.toMutableList()

    private val fullList = employeeList.toMutableList()

    val currentList: List<DataEmployee>
        get() = list

    inner class ViewHolder(val binding: ItemEmployeeBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DataEmployee) {

            binding.tvNameEmployee.text = item.name
            binding.tvCompany.text = item.company
            binding.tvJoinDate.text = DateUtils.formatDate(item.workEntryDate ?: "")

            val avatarUrl = item.avatar
            val fullUrl = if (avatarUrl.isNullOrEmpty()) null else ApiClient.BASE_URL_IMAGE + avatarUrl

            Glide.with(binding.root.context)
                .load(fullUrl)
                .placeholder(R.drawable.ic_people)
                .error(R.drawable.ic_people)
                .into(binding.civEmployee)

            // Click untuk edit / detail
            binding.root.setOnClickListener {
                onClick(item)
            }

            // Popup menu delete
            binding.ivOption.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menuInflater.inflate(R.menu.menu_hapus, popup.menu)

                popup.setOnMenuItemClickListener { menu ->
                    when (menu.itemId) {
                        R.id.action_delete -> {
                            onDelete(item)
                            true
                        }
                        else -> false
                    }
                }

                popup.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEmployeeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    // Update data setelah API refresh
    fun updateData(newList: List<DataEmployee>) {
        list.clear()
        list.addAll(newList)

        fullList.clear()
        fullList.addAll(newList)

        notifyDataSetChanged()
    }

    // 🔍 SEARCH FILTER
    fun filter(query: String) {
        val q = query.lowercase()

        list.clear()

        if (q.isEmpty()) {
            list.addAll(fullList)
        } else {
            list.addAll(
                fullList.filter { emp ->
                    (emp.name ?: "").lowercase().contains(q) ||
                            (emp.company ?: "").lowercase().contains(q)
                }
            )
        }

        notifyDataSetChanged()
    }
}
