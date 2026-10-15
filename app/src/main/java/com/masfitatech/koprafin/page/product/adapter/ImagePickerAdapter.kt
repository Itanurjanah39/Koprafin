package com.masfitatech.koprafin.page.product.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.masfitatech.koprafin.databinding.ItemImageAddBinding
import com.masfitatech.koprafin.databinding.ItemImagePreviewBinding

class ImagePickerAdapter(
    private val images: MutableList<Uri>,
    private val onAddClick: () -> Unit,
    private val onDelete: (Uri) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_IMAGE = 1
    private val TYPE_ADD = 2

    override fun getItemCount(): Int =
        images.size + if (images.size < 6) 1 else 0

    override fun getItemViewType(position: Int): Int =
        if (position < images.size) TYPE_IMAGE else TYPE_ADD

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == TYPE_IMAGE) {
            val binding = ItemImagePreviewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ImageViewHolder(binding)
        } else {
            val binding = ItemImageAddBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            AddViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImageViewHolder) {
            holder.bind(images[position])
        } else if (holder is AddViewHolder) {
            holder.bind()
        }
    }

    inner class ImageViewHolder(private val binding: ItemImagePreviewBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri) {
            Glide.with(binding.root.context)
                .load(uri)
                .centerCrop()
                .into(binding.ivPreview)

            binding.ivDelete.setOnClickListener {
                onDelete(uri)
            }
        }
    }

    inner class AddViewHolder(private val binding: ItemImageAddBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.ivAdd.setOnClickListener { onAddClick() }
        }
    }
}
