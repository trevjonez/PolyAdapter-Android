@file:JvmName("PolyListItemProvider")

package com.trevjonez.polyadapter.providers

import androidx.recyclerview.widget.*
import com.trevjonez.polyadapter.PolyAdapter

/**
 * @param contentsChanged function called once the last diff callback has been delivered to the attached update callback
 */
class PolyListItemProvider(
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