package com.dicoding.storyapp.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.UserRepository
import com.dicoding.storyapp.data.api.ErrorResponse
import com.dicoding.storyapp.data.api.LoginResponse
import com.dicoding.storyapp.data.api.RegisterResponse
import com.dicoding.storyapp.data.pref.UserModel
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.utils.EspressoIdlingResource
import com.dicoding.storyapp.utils.wrapEspressoIdlingResource
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainViewModel(
    private val repository: UserRepository,
    private val userPreference: UserPreference
) : ViewModel() {
    private val _registerResponse = MutableLiveData<RegisterResponse>()
    val registerResponse: LiveData<RegisterResponse> = _registerResponse

    private val _loginResponse = MutableLiveData<LoginResponse>()
    val loginResponse: LiveData<LoginResponse> = _loginResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
        if (isLoading) {
            EspressoIdlingResource.increment()
        } else {
            EspressoIdlingResource.decrement()
        }
    }

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true
        wrapEspressoIdlingResource {
            viewModelScope.launch {
                try {
                    val response = repository.registerUser(name, email, password)
                    _registerResponse.postValue(response)
                } catch (e: Exception) {
                    _registerResponse.postValue(
                        RegisterResponse(
                            true,
                            "Unexpected error: ${e.localizedMessage}"
                        )
                    )
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }


    fun login(email: String, password: String) {
        _isLoading.value = true
        wrapEspressoIdlingResource {
            viewModelScope.launch {
                try {
                    val response = repository.loginUser(email, password)
                    if (!response.error!!) {
                        val user = UserModel(
                            email = email,
                            token = response.loginResult?.token ?: "",
                            isLogin = true
                        )
                        saveSession(user)
                    }
                    _loginResponse.postValue(response)
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                    _loginResponse.postValue(
                        LoginResponse(
                            error = true,
                            message = errorResponse.message ?: "Login Failed"
                        )
                    )
                } catch (e: IOException) {
                    _loginResponse.postValue(
                        LoginResponse(
                            error = true,
                            message = "Network Error, Check your Internet Connection."
                        )
                    )
                } catch (e: Exception) {
                    _loginResponse.postValue(
                        LoginResponse(
                            error = true,
                            message = "Unknown Error has Occurred."
                        )
                    )
                } finally {
                    _isLoading.postValue(false)
                }
            }
        }
    }

    fun getSession(): LiveData<UserModel> {
        return userPreference.getSession().asLiveData()
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            userPreference.saveSession(user)
        }
    }

}