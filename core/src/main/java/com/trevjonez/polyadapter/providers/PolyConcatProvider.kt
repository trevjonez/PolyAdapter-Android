@file:JvmName("PolyConcatProvider")
package com.trevjonez.polyadapter.providers

import android.support.v4.util.SimpleArrayMap
import android.support.v7.util.DiffUtil
import android.support.v7.util.ListUpdateCallback
import com.trevjonez.polyadapter.PolyAdapter

class PolyConcatProvider(vararg val providers: PolyAdapter.ItemProvider) : PolyAdapter.ItemProvider {

  private val offsetCache = SimpleArrayMap<PolyAdapter.ItemProvider, Int>(providers.size)

  inline fun <reified T : PolyAdapter.ItemProvider> getProvider(index: Int): T {
    val rawProvider = providers[index]
    return rawProvider as? T ?: throw UnsupportedOperationException(
        "itemProvider at index $index " +
            "was type ${rawProvider.javaClass.simpleName} " +
            "but expected provider of type ${T::class.java.simpleName}"
    )
  }

  private fun currentOffset(provider: PolyAdapter.ItemProvider): Int {
    if (providers[0] === provider) return 0

    if (offsetCache.containsKey(provider)) return offsetCache[provider]

    val previous = previousProvider(provider)
    val pOffset = currentOffset(previous)
    val current = pOffset + previous.getItemCount()
    offsetCache.put(provider, current)
    return current
  }

  private fun previousProvider(provider: PolyAdapter.ItemProvider): PolyAdapter.ItemProvider {
    val current = providers.indexOf(provider)
    return providers[current - 1]
  }

  private fun invalidateOffsets(provider: PolyAdapter.ItemProvider) {
    var found = false
    providers.forEach {
      if (found) {
        offsetCache.remove(it)
      }
      if (it === provider) found = true
    }
  }

  override fun getItemCount(): Int {
    return providers.sumBy { it.getItemCount() }
  }

  override fun getItem(position: Int): Any {
    var itemOffset = 0
    var providerOffset = 0
    while (providerOffset < providers.size) {
      val providerCount = providers[providerOffset].getItemCount()
      if (position < providerCount + itemOffset) {
        return providers[providerOffset].getItem(position - itemOffset)
      }
      itemOffset += providerCount
      providerOffset++
    }

    throw IndexOutOfBoundsException()
  }

  override fun onAttach(listUpdateCallback: ListUpdateCallback, itemCallback: DiffUtil.ItemCallback<Any>) {
    providers.forEach { provider ->
      //posting updates should always happen from the main thread so synchronization 'should not' be an issue here
      provider.onAttach(object : ListUpdateCallback {
        override fun onChanged(position: Int, count: Int, payload: Any?) {
          listUpdateCallback.onChanged(position + currentOffset(provider), count, payload)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
          val offset = currentOffset(provider)
          listUpdateCallback.onMoved(fromPosition + offset, toPosition + offset)
        }

        override fun onInserted(position: Int, count: Int) {
          invalidateOffsets(provider)
          listUpdateCallback.onInserted(position + currentOffset(provider), count)
        }

        override fun onRemoved(position: Int, count: Int) {
          invalidateOffsets(provider)
          listUpdateCallback.onRemoved(position + currentOffset(provider), count)
        }
      }, itemCallback)
    }
  }
}
