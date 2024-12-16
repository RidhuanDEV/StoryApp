package com.dicoding.storyapp.signup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.databinding.ActivitySignUpBinding
import com.dicoding.storyapp.login.LoginActivity
import com.dicoding.storyapp.main.MainViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: MainViewModel

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()

        lifecycleScope.launch {
            val factory = ViewModelFactory.getInstance(this@SignUpActivity)
            viewModel = ViewModelProvider(this@SignUpActivity, factory)[MainViewModel::class.java]

            observeViewModel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showSnackbar("Please fill in all fields.", false)
                return@setOnClickListener
            }

            if (password.length >= 8) {
                viewModel.register(name, email, password)
            } else {
                showSnackbar("Password must be at least 8 characters long.", false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showSnackbar(message: String, isSuccess: Boolean) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(
            if (isSuccess) getColor(android.R.color.holo_green_dark)
            else getColor(android.R.color.holo_red_dark)
        )
        snackbar.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.registerResponse.observe(this) { response ->
            viewModel.setLoading(false)
            if (response.error == false) {
                showSnackbar("Account created successfully! Please log in", true)
                Handler(mainLooper).postDelayed({
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }, 3000)
            } else {
                showSnackbar(response.message ?: "Registration failed", false)
            }
        }
    }
}
