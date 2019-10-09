package polyadapter.provider

import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.PositionalDataSource
import androidx.recyclerview.widget.ListUpdateCallback
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import polyadapter.equalityItemCallback
import java.util.concurrent.Executor

@RunWith(JUnit4::class)
class PagedListProviderTest {
  data class Foo(val value: Int)

  private val justRunIt = Executor { it.run() }

  private fun source(totalSize: Int): PositionalDataSource<Foo> =
    object : PositionalDataSource<Foo>() {
      override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Foo>) {
        val data = if (params.requestedStartPosition >= totalSize)
          emptyList()
        else
          params.requestedRange()
            .coerceEnd(totalSize - 1)
            .map { Foo(it) }

        callback.onResult(data, params.requestedStartPosition, totalSize)
      }

      override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Foo>) {
        val data = if (params.startPosition >= totalSize)
          emptyList()
        else
          params.requestedRange()
            .coerceEnd(totalSize - 1)
            .map { Foo(it) }

        callback.onResult(data)
      }
    }

  @Mock
  lateinit var listUpdateCallback: ListUpdateCallback

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)
  }

  @Test
  fun `item provider dispatches inserts on first list`() {
    //Given:
    val pagedProvider = PagedListProvider().apply {
      onAttach(listUpdateCallback, equalityItemCallback())
    }
    val firstList = PagedList(source(50), Config(10), justRunIt, justRunIt)

    //When:
    pagedProvider.updateItems(firstList)()()

    //Then:
    verify(listUpdateCallback).onInserted(0, 50)
  }

  @Test
  fun `range loads trigger an data change signal`() {
    //Given:
    val pagedProvider = PagedListProvider().apply {
      onAttach(listUpdateCallback, equalityItemCallback())
    }
    val firstList = PagedList(source(50), Config(10), justRunIt, justRunIt)

    //When:
    pagedProvider.updateItems(firstList)()()
    firstList.loadAround(30)

    //Then:
    verify(listUpdateCallback).onChanged(30, 10, null)
  }

  @Test
  fun `new list triggers a remove signal`() {
    //Given:
    val pagedProvider = PagedListProvider().apply {
      onAttach(listUpdateCallback, equalityItemCallback())
    }
    val firstList = PagedList(source(50), Config(10), justRunIt, justRunIt)
    val secondList = PagedList(source(30), Config(10), justRunIt, justRunIt)

    //When:
    pagedProvider.updateItems(firstList)()()
    pagedProvider.updateItems(secondList)()()

    //Then:
    verify(listUpdateCallback).onRemoved(30, 20)
  }
}


private fun IntRange.coerceEnd(maxEndValue: Int): IntRange {
  return if (endInclusive in start..maxEndValue) this
  else IntRange(start, kotlin.math.max(start, kotlin.math.min(endInclusive, maxEndValue)))
}

private fun PositionalDataSource.LoadInitialParams.requestedRange(): IntRange {
  return requestedStartPosition until (requestedStartPosition + requestedLoadSize)
}

private fun PositionalDataSource.LoadRangeParams.requestedRange(): IntRange {
  return startPosition until (startPosition + loadSize)
}
