package com.dicoding.storyapp.data

import androidx.lifecycle.liveData
import com.dicoding.storyapp.data.api.ApiService
import com.dicoding.storyapp.data.api.ErrorResponse
import com.dicoding.storyapp.data.api.FileUploadResponse
import com.dicoding.storyapp.data.api.LoginResponse
import com.dicoding.storyapp.data.api.RegisterResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

class UserRepository private constructor(
    private val apiService: ApiService
) {

    suspend fun registerUser(name: String, email: String, password: String): RegisterResponse {
        return try {
            apiService.register(name, email, password) // Jika sukses, langsung return hasil
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            RegisterResponse(true, errorResponse.message ?: "Unknown error occurred") // Kembalikan error dengan format response
        } catch (e: IOException) {

            RegisterResponse(true, "Network error: ${e.localizedMessage}")
        }
    }
    suspend fun loginUser(email: String, password: String): LoginResponse {
        return apiService.login(email, password)
    }

    fun uploadImage(imageFile: File, description: String) = liveData {
        emit(Result.Loading)
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            requestImageFile
        )
        try {
            val successResponse = apiService.uploadImage(multipartBody, requestBody)
            emit(Result.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, FileUploadResponse::class.java)
            emit(errorResponse.message?.let { Result.Error(it) })
        }

    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(apiService: ApiService): UserRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UserRepository(apiService)
                INSTANCE = instance
                instance
            }
        }
    }
}