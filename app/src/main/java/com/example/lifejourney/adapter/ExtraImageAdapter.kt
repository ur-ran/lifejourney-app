package com.example.lifejourney.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lifejourney.R
import java.io.File

class ExtraImageAdapter(private val imageUris: List<String>) :
    RecyclerView.Adapter<ExtraImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivExtraImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_extra_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(File(imageUris[position]))
            .placeholder(R.drawable.ic_image_placeholder)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imageUris.size
}
