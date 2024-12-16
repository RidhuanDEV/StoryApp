    package com.dicoding.storyapp.data

    import androidx.paging.PagingSource
    import androidx.paging.PagingState
    import com.dicoding.storyapp.data.api.ApiService
    import com.dicoding.storyapp.data.api.ListStoryItem

    class StoryPagingSource(private val apiService: ApiService) : PagingSource<Int,ListStoryItem>() {
        private companion object {
            const val INITIAL_PAGE_INDEX = 1
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
            return try {
                val position = params.key ?: INITIAL_PAGE_INDEX
                val response = apiService.getStories(position, params.loadSize)
                val responseData = response.listStory?.filterNotNull() ?: emptyList()
                LoadResult.Page(
                    data = responseData,
                    prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                    nextKey = if (responseData.isEmpty()) null else position + 1
                )
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        }
    }