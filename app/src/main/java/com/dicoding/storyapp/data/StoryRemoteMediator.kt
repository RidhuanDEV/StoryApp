package com.dicoding.storyapp.data


import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dicoding.storyapp.data.api.ApiConfig
import com.dicoding.storyapp.data.database.RemoteKeys
import com.dicoding.storyapp.data.database.entity.ListStoryEntity
import com.dicoding.storyapp.data.database.room.StoryDatabase
import com.dicoding.storyapp.data.pref.UserPreference
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalPagingApi::class)
class QuoteRemoteMediator(
    private val database: StoryDatabase,
    private val userPreference: UserPreference
) : RemoteMediator<Int, ListStoryEntity>() {
    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH ->{
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val token = userPreference.getToken().first()
            val responseData = page.let { ApiConfig.getApiService(token).getStories(it, state.config.pageSize) }
            val endOfPaginationReached = responseData.listStory.isNullOrEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().deleteRemoteKeys()
                    database.storyDao().deleteAll()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1


                responseData.listStory?.let { list ->
                    val storyEntities = list.filterNotNull().map { story ->
                        ListStoryEntity(
                            id = story.id ?: "",
                            name = story.name,
                            description = story.description,
                            lat = story.lat,
                            lon = story.lon,
                            createdAt = story.createdAt,
                            photoUrl = story.photoUrl
                        )
                    }
                    val keys = list.filterNotNull().map { story ->
                        RemoteKeys(
                            id = story.id ?: "",
                            prevKey = prevKey,
                            nextKey = nextKey
                        )
                    }


                    database.remoteKeysDao().insertAll(keys)
                    database.storyDao().insertList(storyEntities)
                }
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ListStoryEntity>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }
    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ListStoryEntity>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }
    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ListStoryEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeysDao().getRemoteKeysId(id)
            }
        }
    }



}