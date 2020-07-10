package polyadapter.provider

import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.PositionalDataSource
import androidx.recyclerview.widget.ListUpdateCallback
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
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

  private val listUpdateCallback = object : ListUpdateCallback {
    val changed = mutableListOf<Triple<Int, Int, Any?>>()
    override fun onChanged(position: Int, count: Int, payload: Any?) {
      changed.add(Triple(position, count, payload))
    }

    val moved = mutableListOf<Pair<Int, Int>>()
    override fun onMoved(fromPosition: Int, toPosition: Int) {
      moved.add(fromPosition to toPosition)
    }

    val inserted = mutableListOf<Pair<Int, Int>>()
    override fun onInserted(position: Int, count: Int) {
      inserted.add(position to count)
    }

    val removed = mutableListOf<Pair<Int, Int>>()
    override fun onRemoved(position: Int, count: Int) {
      removed.add(position to count)
    }

    fun reset() {
      changed.clear()
      moved.clear()
      inserted.clear()
      removed.clear()
    }
  }

  @Before
  fun setUp() {
    listUpdateCallback.reset()
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
    assertThat(listUpdateCallback.inserted).contains(0 to 50)
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
    assertThat(listUpdateCallback.changed).contains(Triple(30, 10, null))
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
    assertThat(listUpdateCallback.removed).contains(30 to 20)
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
