package polyadapter

import android.os.Looper
import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import dagger.Binds
import dagger.Module
import java.lang.System.identityHashCode

/**
 * Default list provider encapsulates the basic flow for data swapping
 */
class ListProvider(defaultItems: List<Any> = emptyList()) : PolyAdapter.ItemProvider {

  @Module
  abstract class AsItemProvider {
    @Binds
    abstract fun itemProvider(impl: ListProvider): PolyAdapter.ItemProvider
  }

  private lateinit var listUpdateCallback: ListUpdateCallback
  private lateinit var itemCallback: DiffUtil.ItemCallback<Any>
  private var items: List<Any> = defaultItems.toList()

  override fun getItemCount() = items.size
  override fun getItem(position: Int) = items[position]
  override fun onAttach(
    listUpdateCallback: ListUpdateCallback,
    itemCallback: DiffUtil.ItemCallback<Any>
  ) {
    this.listUpdateCallback = listUpdateCallback
    this.itemCallback = itemCallback
  }

  /**
   * @return A function that will calculate against the current list when updateItems was called and the newItems param.
   * The result of the diff calculation function will return a function that is then used to apply
   * the diff util result and swap the list within the adapter.
   */
  fun updateItems(newItems: List<Any>): () -> (() -> Unit) {
    check(onMainThread()) { "DiffResult Worker must be created on the main thread." }

    val oldItems = items
    return {
      if (onMainThread())
        Log.w(
          "ListProvider",
          "DiffResult processing should be ran on a background thread to avoid blocking the main thread."
        )

      val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
        override fun getOldListSize() = oldItems.size

        override fun getNewListSize() = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
          itemCallback.areItemsTheSame(oldItems[oldItemPosition], newItems[newItemPosition])

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
          itemCallback.areContentsTheSame(oldItems[oldItemPosition], newItems[newItemPosition])

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int) =
          itemCallback.getChangePayload(oldItems[oldItemPosition], newItems[newItemPosition])
      }, true)

      val swapDataAndDispatchDiffResult: () -> Unit = {
        check(onMainThread()) { "Data swap and diffResult dispatching must be called from the main thread" }
        if (items !== oldItems) {
          Log.w("ListProvider", "Inconsistent ordering of diff result application detected.\n" +
            "Trying to apply result of comparison Base@${identityHashCode(oldItems)} -> Head@${identityHashCode(newItems)}\n" +
            "to Base@${identityHashCode(items)}, ")
        } else {
          items = newItems
          diffResult.dispatchUpdatesTo(listUpdateCallback)
        }
      }

      swapDataAndDispatchDiffResult
    }
  }

  private fun onMainThread() =
    Looper.myLooper() == Looper.getMainLooper()
}
