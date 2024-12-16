package com.dicoding.storyapp

import com.dicoding.storyapp.data.database.entity.ListStoryEntity

object DataDummy {
    fun generateDummyStory(): List<ListStoryEntity> {
        val stories: MutableList<ListStoryEntity> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryEntity(
                id = "$i",
                name = "Story $i",
                description = "Description $i",
                photoUrl = "https://picsum.photos/200/300?random=$i",
                lat = -6.20000 + "$i".toDouble(),
                lon = 106.816666 + "$i".toDouble(),
                createdAt = "200{$i}-08-15T09:00:00Z"
            )
            stories.add(story)
        }
        return stories
    }
}