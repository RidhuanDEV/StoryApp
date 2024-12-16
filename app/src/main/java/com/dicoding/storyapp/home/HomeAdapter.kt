package com.dicoding.storyapp.home

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.database.entity.ListStoryEntity
import com.dicoding.storyapp.databinding.ItemStoryBinding
import com.dicoding.storyapp.home.detail.DetailStoryActivity

class HomeAdapter(private val onLoading: (Boolean) -> Unit)
    : PagingDataAdapter<ListStoryEntity, HomeAdapter.MyViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryEntity>() {
            override fun areItemsTheSame(oldItem: ListStoryEntity, newItem: ListStoryEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ListStoryEntity, newItem: ListStoryEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
    inner class MyViewHolder(private val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryEntity, onLoading: (Boolean) -> Unit) {
            binding.progressBar.visibility = View.VISIBLE

            Glide.with(binding.ivItemPhoto.context)
                .load(story.photoUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .listener(object : RequestListener<Drawable> {
                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.visibility = View.GONE
                        return false
                    }
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.visibility = View.GONE
                        return false
                    }
                })
                .into(binding.ivItemPhoto)

            binding.tvItemName.text = story.name

            binding.cardView.setOnClickListener {
                onLoading(true)
                val context = itemView.context
                val intent = Intent(context, DetailStoryActivity::class.java)
                intent.putExtra("story_data_list", story)
                val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    context as Activity,
                    Pair(binding.ivItemPhoto, "image"),
                    Pair(binding.tvItemName, "name")
                )
                context.startActivity(intent, optionsCompat.toBundle())
                onLoading(false)
            }
        }


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it, onLoading) }
    }


}