package com.dicoding.storyapp.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.storyapp.data.UserRepository
import com.dicoding.storyapp.data.database.entity.ListStoryEntity
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.launch

class HomeViewModel(private val userPreference: UserPreference, private val repository: UserRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    val stories: LiveData<PagingData<ListStoryEntity>> =
        repository.getStory().cachedIn(viewModelScope)


    fun logout(){
        viewModelScope.launch {
            wrapEspressoIdlingResource {
            userPreference.logout()
            }
        }
    }

}
