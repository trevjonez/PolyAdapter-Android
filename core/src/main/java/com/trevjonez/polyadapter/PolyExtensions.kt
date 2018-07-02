@file:JvmName("PolyExtensions")

package com.trevjonez.polyadapter

import android.arch.paging.PagedList
import com.trevjonez.polyadapter.providers.PolyConcatProvider
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
 * Blindly cast the itemProvider to a [PolyConcatProvider] and cast the sub provider
 * to type [T] to allow for clean updates of any sub type of provider.
 *
 * Usage examples:
 * ```kotlin
 * updateConcatProvider<PolyListItemProvider>(0) { updateList(items) }
 * ```
 *
 * ```kotlin
 * updateConcatProvider<PolyPagedListProvider>(1) { updateList(items) }
 * ```
 */
inline fun <reified T : PolyAdapter.ItemProvider> PolyAdapter.updateConcatProvider(
    providerIndex: Int,
    crossinline block: T.() -> Unit
) {
  provider<PolyConcatProvider>().getProvider<T>(providerIndex).block()
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
