@file:JvmName("PolyExtensions")

package com.trevjonez.polyadapter

import androidx.paging.PagedList
import com.trevjonez.polyadapter.providers.PolyListItemProvider
import com.trevjonez.polyadapter.providers.PolyPagedListProvider

/**
 * Blindly cast the itemProvider to a [PolyListItemProvider] to make list updates easier
 */
fun PolyAdapter.updateList(items: List<Any>) {
  provider<PolyListItemProvider>().updateList(items)
}

/**
 * Blindly cast the itemProvider to a [PolyPagedListProvider] to make list updates easier
 */
fun PolyAdapter.updatePagedList(items: PagedList<Any>) {
  provider<PolyPagedListProvider>().updateList(items)
}

/**
 * Blindly cast the itemProvider to [T]
 */
inline fun <reified T : PolyAdapter.ItemProvider> PolyAdapter.provider(): T {
  return itemProvider as? T
      ?: throw UnsupportedOperationException(
          "itemProvider was type ${itemProvider.javaClass.simpleName} " +
              "but expected ${T::class.java.simpleName}"
      )
}
