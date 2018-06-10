package com.trevjonez.polyadapter.providers

import android.arch.paging.*
import android.support.v7.util.AdapterListUpdateCallback
import android.support.v7.util.DiffUtil
import android.support.v7.util.ListUpdateCallback
import com.trevjonez.polyadapter.PolyAdapter
import com.trevjonez.polyadapter.diffutil.BackgroundPool
import com.trevjonez.polyadapter.diffutil.CancelDiffException
import com.trevjonez.polyadapter.diffutil.MainThread
import java.util.concurrent.Executor

class PolyPagedListProvider(
    private val paddingItem: Any = Unit,
    private val backgroundExecutor: Executor = BackgroundPool,
    private val mainThreadExecutor: Executor = MainThread
) : PolyAdapter.ItemProvider {

  private lateinit var listUpdateCallback: AdapterListUpdateCallback
  private lateinit var itemCallback: DiffUtil.ItemCallback<Any>

  private var updateCount = 0
  private var isContiguous: Boolean = false
  private var pagedList: PagedList<Any>? = null
  private var snapshot: PagedList<Any>? = null

  override fun onAttach(adapter: PolyAdapter) {
    listUpdateCallback = AdapterListUpdateCallback(adapter)
    itemCallback = adapter.itemCallback
  }

  override fun getItemCount(): Int {
    return pagedList?.size ?: snapshot?.size ?: 0
  }

  override fun getItem(position: Int): Any {
    val pagedListLocal = pagedList
    return if (pagedListLocal != null) {
      pagedListLocal.loadAround(position)
      pagedListLocal[position] ?: paddingItem
    } else {
      val snapshotLocal = snapshot
      if (snapshotLocal == null) {
        throw IndexOutOfBoundsException("Item Provider is empty. $position")
      } else {
        snapshotLocal[position] ?: paddingItem
      }
    }
  }

  private val updateCallback = object : PagedList.Callback() {
    override fun onChanged(position: Int, count: Int) {
      listUpdateCallback.onChanged(position, count, null)
    }

    override fun onInserted(position: Int, count: Int) {
      listUpdateCallback.onInserted(position, count)
    }

    override fun onRemoved(position: Int, count: Int) {
      listUpdateCallback.onRemoved(position, count)
    }
  }

  fun updateList(newList: PagedList<Any>) {
    if (pagedList == null && snapshot == null) {
      isContiguous = newList.contiguous
    } else {
      if (newList.contiguous != isContiguous) {
        throw IllegalArgumentException(
            "PolyPagedListProvider can't handle a mix of " +
                "contiguous and non-contiguous lists."
        )
      }
    }

    if (pagedList == newList) return

    val currentUpdateId = ++updateCount

    if (pagedList == null && snapshot == null) {
      pagedList = newList
      newList.addWeakCallback(null, updateCallback)

      listUpdateCallback.onInserted(0, newList.size)
      return
    }

    pagedList?.let {
      it.removeWeakCallback(updateCallback)
      snapshot = it.snapshot() as PagedList<Any>
      pagedList = null
    }

    require(snapshot != null && pagedList == null) {
      "must be in snapshot state to diff"
    }

    val oldSnapshot = snapshot!!
    val newSnapshot = newList.snapshot() as PagedList<Any>

    backgroundExecutor.execute {
      try {
        val oldOffset = oldSnapshot.computeLeadingNulls
        val newOffset = newSnapshot.computeLeadingNulls

        val oldSize = oldSnapshot.size - oldOffset - oldSnapshot.computeTrailingNulls
        val newSize = newSnapshot.size - newOffset - newSnapshot.computeTrailingNulls

        val result = DiffUtil.calculateDiff(
            callback(currentUpdateId, oldSnapshot, oldOffset, newSnapshot, oldSize, newSize)
        )

        mainThreadExecutor.execute {
          if (currentUpdateId == updateCount) {
            require(snapshot != null && pagedList == null) {
              "must be in snapshot state to diff"
            }
            pagedList = newList
            snapshot = null

            postUpdates(oldSnapshot, newList, result)
          }
        }
      } catch (ignore: CancelDiffException) {
      }
    }
  }

  private fun callback(
      currentUpdateId: Int,
      oldSnapshot: PagedList<Any>,
      oldOffset: Int,
      newSnapshot: PagedList<Any>,
      oldSize: Int,
      newSize: Int
  ): DiffUtil.Callback {
    return object : DiffUtil.Callback() {
      override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        if (currentUpdateId < updateCount) throw CancelDiffException()

        val oldItem = oldSnapshot[oldItemPosition + oldOffset]
        val newItem = newSnapshot[newItemPosition + newSnapshot.leadingNulls]
        return if (oldItem == null || newItem == null) {
          null
        } else {
          itemCallback.getChangePayload(oldItem, newItem)
        }
      }

      override fun getOldListSize() = oldSize

      override fun getNewListSize() = newSize

      override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (currentUpdateId < updateCount) throw CancelDiffException()
        val oldItem = oldSnapshot[oldItemPosition + oldOffset]
        val newItem = newSnapshot[newItemPosition + newSnapshot.leadingNulls]
        return when {
          oldItem === newItem -> true
          oldItem == null || newItem == null -> false
          else -> itemCallback.areItemsTheSame(oldItem, newItem)
        }
      }

      override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (currentUpdateId < updateCount) throw CancelDiffException()
        val oldItem = oldSnapshot[oldItemPosition + oldOffset]
        val newItem = newSnapshot[newItemPosition + newSnapshot.leadingNulls]
        return when {
          oldItem === newItem -> true
          oldItem == null || newItem == null -> false
          else -> itemCallback.areContentsTheSame(oldItem, newItem)
        }
      }
    }
  }

  private fun postUpdates(
      oldSnapshot: PagedList<Any>,
      newList: PagedList<Any>,
      result: DiffUtil.DiffResult
  ) {
    val trailingOld = oldSnapshot.computeTrailingNulls
    val trailingNew = newList.computeTrailingNulls
    val leadingOld = oldSnapshot.computeLeadingNulls
    val leadingNew = newList.computeLeadingNulls

    if (trailingOld == 0 && trailingNew == 0 && leadingOld == 0 && leadingNew == 0) {
      result.dispatchUpdatesTo(listUpdateCallback)
      return
    }

    //Add or remove trailing padding
    if (trailingOld > trailingNew) {
      val count = trailingOld - trailingNew
      listUpdateCallback.onRemoved(oldSnapshot.size - count, count)
    } else if (trailingOld < trailingNew) {
      listUpdateCallback.onInserted(oldSnapshot.size, trailingNew - trailingOld)
    }

    //Add or remove leading padding
    if (leadingOld > leadingNew) {
      listUpdateCallback.onRemoved(0, leadingOld - leadingNew)
    } else if (leadingOld < leadingNew) {
      listUpdateCallback.onInserted(0, leadingNew - leadingOld)
    }

    if (leadingNew != 0) {
      result.dispatchUpdatesTo(object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
          listUpdateCallback.onInserted(position + leadingNew, count)
        }

        override fun onRemoved(position: Int, count: Int) {
          listUpdateCallback.onRemoved(position + leadingNew, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
          listUpdateCallback.onMoved(fromPosition + leadingNew, toPosition + leadingNew)
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
          listUpdateCallback.onChanged(position + leadingNew, count, payload)
        }
      })
    } else {
      result.dispatchUpdatesTo(listUpdateCallback)
    }
  }
}