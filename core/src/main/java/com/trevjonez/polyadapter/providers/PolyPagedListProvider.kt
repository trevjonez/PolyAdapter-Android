@file:JvmName("PolyPagedListProvider")

package com.trevjonez.polyadapter.providers

import androidx.paging.AsyncPagedListDiffer
import androidx.paging.PagedList
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.BatchingListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.trevjonez.polyadapter.PolyAdapter

/**
 * PagedList ItemProvider impl via delegation to [AsyncPagedListDiffer]
 *
 * It differs in two ways:
 * 1: It does not expose the `onCurrentListChanged` functionality.
 * 2: It allows customization of the padding object to preserve null safety.
 */
class PolyPagedListProvider(
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