package com.dicoding.storyapp.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.storyapp.R
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.databinding.ActivityHomeBinding
import com.dicoding.storyapp.home.maps.MapsActivity
import com.dicoding.storyapp.home.upload.UploadStoryActivity
import com.dicoding.storyapp.login.LoginActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: ActivityHomeBinding
    private lateinit var storyAdapter: HomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory.getInstance(this@HomeActivity)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]


        setupRecycleView()
        observeViewModel()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_upload -> {
                val intent = Intent(this, UploadStoryActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.maps -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.action_logout -> {
                showLogoutDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.yes) { dialog, id ->
                viewModel.logout()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton(R.string.no) { dialog, id ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.stories.observe(this) { story ->
            if (story != null) {
                storyAdapter.submitData(lifecycle, story)
            } else {
                Toast.makeText(this, "Data is Empty", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun setupRecycleView() {
        storyAdapter = HomeAdapter { isLoading ->
            showLoading(isLoading)
        }
        binding.rvEvent.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = storyAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter { storyAdapter.retry() }
            )
        }
        storyAdapter.addLoadStateListener { loadState ->
            binding.progressBar.visibility = if (loadState.source.refresh is LoadState.Loading) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (loadState.source.refresh is LoadState.Error) {
                val errorState = loadState.source.refresh as LoadState.Error
                Toast.makeText(this, "Source Error : ${errorState.error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }
}