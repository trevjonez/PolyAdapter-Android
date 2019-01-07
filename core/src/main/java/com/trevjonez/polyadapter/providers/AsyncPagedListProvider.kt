package com.trevjonez.polyadapter.providers

import androidx.paging.AsyncPagedListDiffer
import androidx.paging.PagedList
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.BatchingListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.trevjonez.polyadapter.PolyAdapter

/**
 * Blindly cast the itemProvider to a [AsyncPagedListProvider] to make list updates easier
 */
fun PolyAdapter.updatePagedList(items: PagedList<Any>) = provider<AsyncPagedListProvider>().updateList(items)

@Deprecated("Class renamed to more closely describe its implementation",
    ReplaceWith("AsyncPagedListProvider(paddingItem, contentsChanged)",
        "com.trevjonez.polyadapter.providers.AsyncPagedListProvider"),
    DeprecationLevel.ERROR)
fun PolyPagedListProvider(paddingItem: Any = Unit, contentsChanged: (() -> Unit)? = null) =
    AsyncPagedListProvider(paddingItem, contentsChanged)

/**
 * PagedList ItemProvider impl via delegation to [AsyncPagedListDiffer]
 *
 * It differs in two ways:
 * 1: It does not expose the `onCurrentListChanged` functionality.
 * 2: It allows customization of the padding object to preserve null safety.
 */
class AsyncPagedListProvider(
    private val paddingItem: Any = Unit,
    private val contentsChanged: (() -> Unit)? = null
) : PolyAdapter.ItemProvider {

  private lateinit var listDiffer: AsyncPagedListDiffer<Any>

  override fun onAttach(listUpdateCallback: ListUpdateCallback, itemCallback: DiffUtil.ItemCallback<Any>) {
    val finalDetector = object : BatchingListUpdateCallback(listUpdateCallback) {
      override fun dispatchLastEvent() {
        super.dispatchLastEvent()
        contentsChanged?.invoke()
      }
    }
    listDiffer = AsyncPagedListDiffer(finalDetector, AsyncDifferConfig.Builder(itemCallback).build())
  }

  override fun getItemCount(): Int {
    return listDiffer.itemCount
  }

  override fun getItem(position: Int): Any {
    return listDiffer.getItem(position) ?: paddingItem
  }

  fun updateList(newList: PagedList<Any>) {
    listDiffer.submitList(newList)
  }
}