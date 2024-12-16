package com.dicoding.storyapp.home.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.Result
import com.dicoding.storyapp.data.UserRepository
import com.dicoding.storyapp.data.api.ListStoryResponse
import kotlinx.coroutines.launch

class MapsViewModel(private val repository: UserRepository) : ViewModel() {
    private val _listStoryItem = MutableLiveData<Result<ListStoryResponse>>()
    val listStoryItem: LiveData<Result<ListStoryResponse>> = _listStoryItem

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getAllMarkers() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _listStoryItem.postValue(Result.Loading)
                val result = repository.getMaps()
                _listStoryItem.postValue(result)
            } catch (e: Exception) {
                _listStoryItem.postValue(Result.Error("Unexpected error: ${e.localizedMessage}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
