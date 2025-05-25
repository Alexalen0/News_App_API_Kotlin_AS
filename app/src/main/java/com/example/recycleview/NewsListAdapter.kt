package com.example.recycleview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NewsListAdapter(private val listener: NewsItemClicked) : RecyclerView.Adapter<NewsViewHolder>() {

    private val items: ArrayList<News> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        val viewHolder = NewsViewHolder(view)
        view.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClicked(items[position])
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = items[position]
        holder.titleView.text = currentItem.title
        holder.author.text = if (!currentItem.author.isNullOrEmpty()) "By ${currentItem.author}" else "Unknown author"
        holder.description.text = currentItem.description ?: ""
        holder.source.text = currentItem.source ?: "Unknown"

        val imageUrl = currentItem.urlToImage
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.rounded_image_bg)
                .error(R.drawable.rounded_image_bg)
                .centerCrop()
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.rounded_image_bg)
        }
    }

    fun updateNews(newNews: ArrayList<News>) {
        val diffCallback = object : androidx.recyclerview.widget.DiffUtil.Callback() {
            override fun getOldListSize(): Int = items.size
            override fun getNewListSize(): Int = newNews.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                items[oldItemPosition].url == newNews[newItemPosition].url
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                items[oldItemPosition] == newNews[newItemPosition]
        }
        val diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(diffCallback)
        items.clear()
        items.addAll(newNews)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = items.size
}

class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val titleView: TextView = itemView.findViewById(R.id.title)
    val author: TextView = itemView.findViewById(R.id.author)
    val image: ImageView = itemView.findViewById(R.id.image)
    val description: TextView = itemView.findViewById(R.id.description)
    val source: TextView = itemView.findViewById(R.id.source)
}
