package com.dicoding.storyapp.signup

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.ViewModelFactory
import com.dicoding.storyapp.databinding.ActivitySignUpBinding
import com.dicoding.storyapp.main.MainViewModel
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: MainViewModel

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

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showMessage("Oops!", "Please fill in all fields.")
                return@setOnClickListener
            }

            if (password.length >= 8) {
                viewModel.register(name, email, password)
            } else {
                showMessage("Oops!", "Password must be at least 8 characters long.")
            }
        }
    }

    private fun showMessage(title: String, message: String) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { _, _ ->
                if (title == "Yeah!") {
                    finish()
                }
            }
            create()
            show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.registerResponse.observe(this) { response ->
            viewModel.setLoading(false)
            if (response.error == false) {
                showMessage("Yeah!", "Account with ${binding.edRegisterEmail.text} has been created. Please log in to see your stories.")
            } else {
                showMessage("Oops!", response.message ?: "Registration failed")
            }
        }
    }
}
