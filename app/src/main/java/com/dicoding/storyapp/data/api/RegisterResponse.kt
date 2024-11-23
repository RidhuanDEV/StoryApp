package com.dicoding.storyapp.data.api

import com.google.gson.annotations.SerializedName

data class RegisterResponse(

	@field:SerializedName("error")
	var error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)
