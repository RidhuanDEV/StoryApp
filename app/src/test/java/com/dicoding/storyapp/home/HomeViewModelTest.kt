package com.dicoding.storyapp.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.storyapp.DataDummy
import com.dicoding.storyapp.MainDispatcherRule
import com.dicoding.storyapp.data.UserRepository
import com.dicoding.storyapp.data.database.entity.ListStoryEntity
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest{
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()
    @Mock
    private lateinit var repository: UserRepository
    @Mock
    private lateinit var userPreference: UserPreference

    @Test
    fun `when Get Story, should not null and Return Data`() = runTest {
        val dummyStory = DataDummy.generateDummyStory()
        val data : PagingData<ListStoryEntity> = StoryPagingSource.snapshot(dummyStory)
        val expectedStory = MutableLiveData<PagingData<ListStoryEntity>>()
        expectedStory.value = data
        Mockito.`when`(repository.getStory()).thenReturn(expectedStory)

        val HomeViewModel = HomeViewModel(userPreference, repository)
        val actualStory : PagingData<ListStoryEntity> = HomeViewModel.stories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = HomeAdapter.DIFF_CALLBACK,
            updateCallback = AdapterListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStory)
        assertNotNull(differ.snapshot())
        assertEquals(dummyStory.size, differ.snapshot().size)
        assertEquals(dummyStory[0], differ.snapshot()[0])
    }


    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val dummyStory = DataDummy.generateDummyStory()
        val data : PagingData<ListStoryEntity> = PagingData.from(emptyList())
        val expectedStory = MutableLiveData<PagingData<ListStoryEntity>>()
        expectedStory.value = data
        Mockito.`when`(repository.getStory()).thenReturn(expectedStory)

        val HomeViewModel = HomeViewModel(userPreference, repository)
        val actualStory : PagingData<ListStoryEntity> = HomeViewModel.stories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = HomeAdapter.DIFF_CALLBACK,
            updateCallback = AdapterListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStory)
        assertEquals(0, differ.snapshot().size)
    }

}

class StoryPagingSource : PagingSource<Int, LiveData<List<ListStoryEntity>>>() {
    companion object {
        fun snapshot(items: List<ListStoryEntity>): PagingData<ListStoryEntity> {
            return PagingData.from(items)
        }
    }
    override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStoryEntity>>>): Int {
        return 0
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStoryEntity>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}

val AdapterListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}