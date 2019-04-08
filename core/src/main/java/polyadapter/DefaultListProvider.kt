package polyadapter

import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback

/**
 * Default list provider encapsulates the basic flow for data swapping
 */
class DefaultListProvider(
    defaultItems: List<Any>
) : PolyAdapter.ItemProvider {

  private lateinit var listUpdateCallback: ListUpdateCallback
  private lateinit var itemCallback: DiffUtil.ItemCallback<Any>
  private var items: List<Any> = defaultItems.toList()

  override fun getItemCount() = items.size
  override fun getItem(position: Int) = items[position]
  override fun onAttach(listUpdateCallback: ListUpdateCallback, itemCallback: DiffUtil.ItemCallback<Any>) {
    this.listUpdateCallback = listUpdateCallback
    this.itemCallback = itemCallback
  }

  /**
   * @return A function that will calculate against the current list when updateItems was called and the newItems param.
   * The result of the diff calculation function will return a function that can then be used to apply
   * the diff util result and swap the data list.
   */
  fun updateItems(newItems: List<Any>): () -> (() -> Unit) {
    val oldList = items
    return {
      val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            itemCallback.areItemsTheSame(oldList[oldItemPosition], newItems[newItemPosition])

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            itemCallback.areContentsTheSame(oldList[oldItemPosition], newItems[newItemPosition])

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int) =
            itemCallback.getChangePayload(oldList[oldItemPosition], newItems[newItemPosition])
      }, true)

      val swapDataAndDispatchDiffResult: () -> Unit = {
        if (Looper.myLooper() != Looper.getMainLooper())
          throw IllegalStateException("Data swap and diffResult dispatching must be called from the main thread")
        items = newItems
        diffResult.dispatchUpdatesTo(listUpdateCallback)
      }

      swapDataAndDispatchDiffResult
    }
  }
}