package com.dicoding.storyapp.home.detail

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.storyapp.data.api.ListStoryItem
import com.dicoding.storyapp.databinding.ActivityDetailStoryBinding

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailStoryBinding
    private val itemDataList = "story_data_list"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        val storyItem: ListStoryItem? = intent.getParcelableExtra(itemDataList)
        if (storyItem != null) {
            showLoading(true)
            binding.tvDetailName.text = storyItem.name
            binding.tvStoryTime.text = storyItem.createdAt
            binding.tvDetailDescription.text = storyItem.description
            Glide.with(this)
                .load(storyItem.photoUrl)
                .into(binding.ivDetailPhoto)

        } else {
            handleError()
        }
        showLoading(false)

    }
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }
    private fun handleError() {
        val message = "Data Is Missing !"
        showLoading(false)
        binding.tvDetailName.text = message
    }

}