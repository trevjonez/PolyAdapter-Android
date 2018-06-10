package com.trevjonez.polyadapter.providers

import android.os.Looper
import android.support.v7.util.AdapterListUpdateCallback
import android.support.v7.util.DiffUtil
import com.trevjonez.polyadapter.PolyAdapter
import com.trevjonez.polyadapter.diffutil.BackgroundPool
import com.trevjonez.polyadapter.diffutil.CancelDiffException
import com.trevjonez.polyadapter.diffutil.MainThread
import java.util.concurrent.Executor

class PolyListItemProvider(
    private val backgroundExecutor: Executor = BackgroundPool,
    private val mainThreadExecutor: Executor = MainThread
) : PolyAdapter.ItemProvider {

  private lateinit var listUpdateCallback: AdapterListUpdateCallback
  private lateinit var itemCallback: DiffUtil.ItemCallback<Any>

  private var list: List<Any> = emptyList()
  private var updateCount = 0

  override fun onAttach(adapter: PolyAdapter) {
    listUpdateCallback = AdapterListUpdateCallback(adapter)
    itemCallback = adapter.itemCallback
  }

  override fun getItemCount(): Int {
    return list.size
  }

  override fun getItem(position: Int): Any {
    return requireNotNull(list[position])
  }

  @android.support.annotation.MainThread
  fun updateList(newItems: List<Any>) {
    require(Looper.myLooper() == Looper.getMainLooper())

    val currentUpdateId = ++updateCount

    if (newItems.isEmpty()) {
      val removedCount = list.size
      list = emptyList()
      listUpdateCallback.onRemoved(0, removedCount)
      return
    }

    if (list.isEmpty()) {
      list = newItems
      listUpdateCallback.onInserted(0, list.size)
      return
    }

    val oldList = list

    backgroundExecutor.execute {
      try {

        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
          override fun getOldListSize() = oldList.size

          override fun getNewListSize() = newItems.size

          override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (updateCount > currentUpdateId) throw CancelDiffException()

            return itemCallback.areItemsTheSame(
                oldList[oldItemPosition], newItems[newItemPosition]
            )
          }

          override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (updateCount > currentUpdateId) throw CancelDiffException()

            return itemCallback.areContentsTheSame(
                oldList[oldItemPosition], newItems[newItemPosition]
            )
          }

          override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            if (updateCount > currentUpdateId) throw CancelDiffException()

            return itemCallback.getChangePayload(
                oldList[oldItemPosition], newItems[newItemPosition]
            )
          }
        })

        mainThreadExecutor.execute {
          if (updateCount == currentUpdateId) {
            list = newItems
            result.dispatchUpdatesTo(listUpdateCallback)
          }
        }

      } catch (ignore: CancelDiffException) {
      }
    }
  }
}