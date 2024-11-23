package com.dicoding.storyapp.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.databinding.ActivityLoginBinding
import com.dicoding.storyapp.home.HomeActivity
import com.dicoding.storyapp.main.MainViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: MainViewModel

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
    private fun observeLoginResponse() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.loginResponse.observe(this) { response ->
            if (response != null) {
                viewModel.setLoading(false)
                if (!response.error!!) {
                    Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()

                    // Simpan token ke UserPreference
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
                    Toast.makeText(this, response.message ?: "Login Gagal", Toast.LENGTH_SHORT).show()
                }
            } else {
                viewModel.setLoading(false)
                Toast.makeText(this, "Terjadi kesalahan. Coba lagi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                false
            }

            password.isEmpty() -> {
                Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                false
            }

            else -> true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}