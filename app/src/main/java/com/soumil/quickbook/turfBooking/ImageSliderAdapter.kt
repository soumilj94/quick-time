package com.soumil.quickbook.turfBooking

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.soumil.quickbook.R

class ImageSliderAdapter(private val imageUrls: List<String>) : RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_slide_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]

        // Show progress bar before loading the image
        holder.imageView.visibility = View.INVISIBLE

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .apply(
                RequestOptions()
                    .override(800, 600) // Resize the image
                    .downsample(DownsampleStrategy.AT_LEAST)
                    .fitCenter()
            )
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    holder.imageView.setImageDrawable(resource)
                    holder.imageView.visibility = View.VISIBLE // Show image when loaded
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    holder.imageView.visibility = View.GONE
                }
            })
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }
}