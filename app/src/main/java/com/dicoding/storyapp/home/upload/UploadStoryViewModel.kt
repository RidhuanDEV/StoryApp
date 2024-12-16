package com.dicoding.storyapp.home.upload

import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.UserRepository
import java.io.File

class UploadStoryViewModel(val repository: UserRepository) : ViewModel() {
    fun uploadImage(file: File, description: String, lat: Double?, lon: Double?) = repository.uploadImage( file, description, lat, lon)

}