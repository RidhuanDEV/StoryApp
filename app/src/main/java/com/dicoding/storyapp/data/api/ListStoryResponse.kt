package com.dicoding.storyapp.data.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ListStoryResponse(
	val listStory: List<ListStoryItem?>? = null,
	val error: Boolean? = null,
	val message: String? = null
)

@Parcelize
data class ListStoryItem(
	val photoUrl: String? = null,
	val createdAt: String? = null,
	val name: String? = null,
	val description: String? = null,
	val lon: Double? = null,
	val id: String? = null,
	val lat: Double? = null
) : Parcelable

