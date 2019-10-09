package polyadapter.provider

import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import polyadapter.PolyAdapter

object Placeholder

class PagedListProvider : PolyAdapter.ItemProvider {

  private lateinit var pagedList: PagedList<*>

  private val pagedListCallback: PagedList.Callback = object : PagedList.Callback() {
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

  private lateinit var listUpdateCallback: ListUpdateCallback
  private lateinit var itemCallback: DiffUtil.ItemCallback<Any>

  override fun onAttach(
    listUpdateCallback: ListUpdateCallback,
    itemCallback: DiffUtil.ItemCallback<Any>
  ) {
    this.listUpdateCallback = listUpdateCallback
    this.itemCallback = itemCallback
  }

  override fun getItemCount() = if (::pagedList.isInitialized) pagedList.size else 0

  override fun getItem(position: Int): Any {
    pagedList.loadAround(position)
    return pagedList[position] ?: Placeholder
  }

  /**
   * @return A function that will calculate against the current list when updateItems was called and the newItems param.
   * The result of the diff calculation function will return a function that is then used to apply
   * the diff util result and swap the list within the adapter.
   */
  @Suppress("UNCHECKED_CAST")
  fun updateItems(newItems: PagedList<*>): () -> (() -> Unit) {
    newItems as PagedList<Any>
    return if (!::pagedList.isInitialized) {
      object : () -> (() -> Unit) {
        override fun invoke() = insertFirstList(newItems)
      }
    } else {
      require(DiffHelper.matchingIsContiguous(pagedList, newItems)) {
        "mixing contiguous and non-contiguous data sources is unsupported."
      }

      pagedList.removeWeakCallback(pagedListCallback)

      val previous = pagedList.snapshot() as PagedList<Any>
      val next = newItems.snapshot() as PagedList<Any>

      object : () -> (() -> Unit) {
        override fun invoke(): () -> Unit {

          val diffResult = DiffHelper.computeDiff(previous, next, itemCallback)
          return object : () -> Unit {
            override fun invoke() {
              pagedList = newItems
              DiffHelper.dispatchDiff(listUpdateCallback, previous, newItems, diffResult)
              newItems.addWeakCallback(next, pagedListCallback)
            }
          }
        }
      }
    }
  }

  private fun insertFirstList(newItems: PagedList<*>): () -> Unit = {
    pagedList = newItems
    pagedList.addWeakCallback(null, pagedListCallback)
    listUpdateCallback.onInserted(0, pagedList.size)
  }

  private object DiffHelper {
    private val pagedStorage by lazy {
      Class.forName("androidx.paging.PagedStorage")
    }

    private val pagedStorageDiffHelper by lazy {
      Class.forName("androidx.paging.PagedStorageDiffHelper")
    }

    private val getStorage by lazy {
      PagedList::class.java.getDeclaredField("mStorage").apply {
        isAccessible = true
      }
    }

    private val computeDiff by lazy {
      pagedStorageDiffHelper.getDeclaredMethod(
        "computeDiff",
        pagedStorage,
        pagedStorage,
        DiffUtil.ItemCallback::class.java
      ).apply {
        isAccessible = true
      }
    }

    private val dispatchDiff by lazy {
      pagedStorageDiffHelper.getDeclaredMethod(
        "dispatchDiff",
        ListUpdateCallback::class.java,
        pagedStorage,
        pagedStorage,
        DiffUtil.DiffResult::class.java
      ).apply {
        isAccessible = true
      }
    }

    private val <T> PagedList<T>.storage: Any
      get() = getStorage.get(this)

    private val isContiguous by lazy {
      PagedList::class.java.getDeclaredMethod("isContiguous").apply {
        isAccessible = true
      }
    }

    private fun Any.isContiguous(): Boolean = isContiguous.invoke(this) as Boolean

    fun computeDiff(
      oldList: PagedList<*>,
      newList: PagedList<*>,
      diffCallback: DiffUtil.ItemCallback<*>
    ): DiffUtil.DiffResult {
      return computeDiff(
        null,
        oldList.storage,
        newList.storage,
        diffCallback
      ) as DiffUtil.DiffResult
    }

    fun dispatchDiff(
      updateCallback: ListUpdateCallback,
      oldList: PagedList<*>,
      newList: PagedList<*>,
      diffResult: DiffUtil.DiffResult
    ) {
      dispatchDiff(
        null,
        updateCallback,
        oldList.storage,
        newList.storage,
        diffResult
      )
    }

    fun matchingIsContiguous(
      oldList: PagedList<*>,
      newList: PagedList<*>
    ): Boolean {
      return oldList.isContiguous() == newList.isContiguous()
    }
  }
}
