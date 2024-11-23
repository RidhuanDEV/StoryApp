package com.dicoding.storyapp.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.databinding.ActivityMainBinding
import com.dicoding.storyapp.home.HomeActivity
import com.dicoding.storyapp.login.LoginActivity
import com.dicoding.storyapp.signup.SignUpActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()

        lifecycleScope.launch {
            val factory = ViewModelFactory.getInstance(this@MainActivity)
            viewModel = ViewModelProvider(this@MainActivity, factory)[MainViewModel::class.java]

            showLoading(true)
            viewModel.getSession().observe(this@MainActivity) { user ->
                if (user.isLogin) {
                    startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                    showLoading(false)
                    finish()
                } else {
                    showLoading(false)
                }
            }

            binding.loginButton.setOnClickListener {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
            }

            binding.registerButton.setOnClickListener {
                val intent = Intent(this@MainActivity, SignUpActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }
}
