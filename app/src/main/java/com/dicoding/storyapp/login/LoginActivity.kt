package com.dicoding.storyapp.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.databinding.ActivityLoginBinding
import com.dicoding.storyapp.home.HomeActivity
import com.dicoding.storyapp.main.MainViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: MainViewModel

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val factory = ViewModelFactory.getInstance(this@LoginActivity)
            viewModel = ViewModelProvider(this@LoginActivity, factory)[MainViewModel::class.java]

            observeLoginResponse()

            binding.loginButton.setOnClickListener {
                val email = binding.edLoginEmail.text.toString()
                val password = binding.edLoginPassword.text.toString()

                if (validateInput(email, password)) {
                    viewModel.login(email, password)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun observeLoginResponse() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.loginResponse.observe(this) { response ->
            if (response != null) {
                viewModel.setLoading(false)
                showSnackbar("Login Berhasil", true)
                if (!response.error!!) {

                    lifecycleScope.launch {
                        val user = UserModel(
                            email = binding.edLoginEmail.text.toString(),
                            token = response.loginResult?.token ?: "",
                            isLogin = true
                        )

                        viewModel.saveSession(user)

                        Log.d("LoginActivity", "Token saved successfully")
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    }
                } else {
                    showSnackbar(response.message ?: "User not found", false)
                }
            } else {
                viewModel.setLoading(false)
                showSnackbar("Terjadi kesalahan. Coba lagi.", false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showSnackbar("Email tidak boleh kosong", false)
                false
            }
            password.isEmpty() -> {
                showSnackbar("Password tidak boleh kosong", false)
                false
            }
            else -> true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun showSnackbar(message: String, isSuccess: Boolean) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        if (isSuccess) {
            snackbar.setBackgroundTint(getColor(android.R.color.holo_green_dark))
        } else {
            snackbar.setBackgroundTint(getColor(android.R.color.holo_red_dark))
        }
        snackbar.show()
    }
}
