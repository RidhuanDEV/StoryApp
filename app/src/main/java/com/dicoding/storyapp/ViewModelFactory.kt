package com.dicoding.storyapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.data.UserRepository
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.pref.dataStore
import com.dicoding.storyapp.di.Injection
import com.dicoding.storyapp.home.HomeViewModel
import com.dicoding.storyapp.home.maps.MapsViewModel
import com.dicoding.storyapp.home.upload.UploadStoryViewModel
import com.dicoding.storyapp.main.MainViewModel

class ViewModelFactory private constructor(
    private val userRepository: UserRepository,
    private val userPreference: UserPreference
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(userRepository, userPreference) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->{
                HomeViewModel(userPreference, userRepository) as T
            }
            modelClass.isAssignableFrom(UploadStoryViewModel::class.java) -> {
                UploadStoryViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                MapsViewModel(userRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory {
            val repository = Injection.provideRepository(context)
            val userPreference = UserPreference.getInstance(context.dataStore)
            return INSTANCE ?: synchronized(this) {
                val instance = ViewModelFactory(repository, userPreference)
                INSTANCE = instance
                instance
            }
        }

    }
}