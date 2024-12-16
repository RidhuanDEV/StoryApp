package com.dicoding.storyapp.data


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.storyapp.data.api.ApiConfig
import com.dicoding.storyapp.data.api.ApiService
import com.dicoding.storyapp.data.api.ErrorResponse
import com.dicoding.storyapp.data.api.FileUploadResponse
import com.dicoding.storyapp.data.api.ListStoryResponse
import com.dicoding.storyapp.data.api.LoginResponse
import com.dicoding.storyapp.data.api.RegisterResponse
import com.dicoding.storyapp.data.database.entity.ListStoryEntity
import com.dicoding.storyapp.data.database.room.StoryDatabase
import com.dicoding.storyapp.data.pref.UserPreference
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference,
    private val storyDatabase: StoryDatabase,
) {

    suspend fun registerUser(name: String, email: String, password: String): RegisterResponse {
        return try {
            apiService.register(name, email, password)
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

    fun uploadImage(imageFile: File, description: String, lat: Double?, lon: Double?) = liveData {
        emit(Result.Loading)
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo",
            imageFile.name,
            requestImageFile
        )

        val latBody = lat?.toString()?.toRequestBody("text/plain".toMediaType())
        val lonBody = lon?.toString()?.toRequestBody("text/plain".toMediaType())

        try {
            val token = userPreference.getToken().first()
            val successResponse = ApiConfig.getApiService(token).uploadImage(
                multipartBody,
                requestBody,
                latBody,
                lonBody
            )
            Log.d("API Response", "Response: $successResponse")
            emit(Result.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("API Error", "Error Body: $errorBody")
            val errorResponse = Gson().fromJson(errorBody, FileUploadResponse::class.java)
            emit(errorResponse.message?.let { Result.Error(it) })
        }
    }


    @OptIn(ExperimentalPagingApi::class)
    fun getStory(): LiveData<PagingData<ListStoryEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false
            ),
            remoteMediator = QuoteRemoteMediator(storyDatabase, userPreference),
            pagingSourceFactory = { storyDatabase.storyDao().getAllList() }
        ).liveData
    }

    suspend fun getMaps(): Result<ListStoryResponse> {
        return try {
            val token = userPreference.getToken().first()
            val response = ApiConfig.getApiService(token).getStoriesWithLocation()
            Result.Success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, FileUploadResponse::class.java)
            Result.Error(errorResponse.message ?: "Unknown error")
        } catch (e: Exception) {
            Result.Error("Unexpected error: ${e.localizedMessage}")
        }
    }


    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(apiService: ApiService, userPreference: UserPreference, storyDatabase: StoryDatabase): UserRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UserRepository(apiService,userPreference, storyDatabase)
                INSTANCE = instance
                instance
            }
        }
    }
}