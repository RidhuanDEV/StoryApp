package com.dicoding.storyapp.data.database.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.storyapp.data.database.entity.ListStoryEntity

@Dao
interface ListStoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(story: List<ListStoryEntity>)

    @Query("SELECT * FROM story ORDER BY createdAt DESC")
    fun getAllList(): PagingSource<Int, ListStoryEntity>


    @Query("DELETE FROM story")
    suspend fun deleteAll()
}