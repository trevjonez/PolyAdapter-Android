package com.trevjonez.polyadapter.providers

import androidx.recyclerview.widget.*
import com.trevjonez.polyadapter.PolyAdapter

/**
 * Blindly cast the itemProvider to a [AsyncListProvider] to make list updates easier
 */
fun PolyAdapter.updateList(items: List<Any>) = provider<AsyncListProvider>().updateList(items)


@Deprecated("Class renamed to more closely describe its implementation",
    ReplaceWith("AsyncListProvider(contentsChanged)",
        "com.trevjonez.polyadapter.providers.AsyncListProvider"),
    DeprecationLevel.ERROR)
fun PolyListItemProvider(paddingItem: Any = Unit, contentsChanged: (() -> Unit)? = null) =
    AsyncPagedListProvider(paddingItem, contentsChanged)

/**
 * List ItemProvider impl via delegation to [AsyncListDiffer]
 * @param contentsChanged function called once the last diff callback has been delivered to the attached update callback
 */
class AsyncListProvider(
    private val contentsChanged: (() -> Unit)? = null
) : PolyAdapter.ItemProvider {

  private lateinit var listDiffer: AsyncListDiffer<Any>

  override fun onAttach(listUpdateCallback: ListUpdateCallback, itemCallback: DiffUtil.ItemCallback<Any>) {
    val finalDetector = object : BatchingListUpdateCallback(listUpdateCallback) {
      override fun dispatchLastEvent() {
        super.dispatchLastEvent()
        contentsChanged?.invoke()
      }
    }

    listDiffer = AsyncListDiffer(finalDetector, AsyncDifferConfig.Builder(itemCallback).build())
  }

  override fun getItemCount(): Int {
    return listDiffer.currentList.size
  }

  override fun getItem(position: Int): Any {
    return requireNotNull(listDiffer.currentList[position])
  }

  fun updateList(list: List<Any>) {
    listDiffer.submitList(list)
  }
}