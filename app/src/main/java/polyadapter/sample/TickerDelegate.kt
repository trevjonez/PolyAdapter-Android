package polyadapter.sample

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import polyadapter.PolyAdapter
import polyadapter.ScopedDelegate
import polyadapter.ScopedDelegate.Companion.default
import polyadapter.equalityItemCallback
import polyadapter.sample.databinding.TickerItemBinding
import java.lang.System.identityHashCode
import java.util.*
import javax.inject.Inject

/**
 * Example of using a scoped delegate to manage coroutine jobs within the bounds
 * of a view holders lifecycle. Using the provided default implementation of a [ScopedDelegate]
 */
class TickerDelegate @Inject constructor(
  hostLifecycle: Lifecycle
) : PolyAdapter.BindingDelegate<Ticker, TickerHolder>,
  ScopedDelegate by default(hostLifecycle.coroutineScope) {

  override val layoutId = R.layout.ticker_item
  override val dataType = Ticker::class.java
  override val itemCallback: DiffUtil.ItemCallback<Ticker> = equalityItemCallback()
  override fun createViewHolder(itemView: View) = TickerHolder(itemView)
  override fun bindView(holder: TickerHolder, item: Ticker) {
    val flowJob = callbackFlow {
      var count = 0
      invokeOnClose { Log.i("TickerFlow", "Closed at count: $count") }
      while (true) {
        send(count)
        count++
        delay(1000)
      }
    }
      .onEach { holder.updateTicker(it) }
      .launchIn(holder.coroutineScope)

    holder.setJobHash(identityHashCode(flowJob))
  }
}

class Ticker

class TickerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  private val tickerBinding = TickerItemBinding.bind(itemView)

  fun updateTicker(count: Int) {
    tickerBinding.tickerCount.text = "$count"
  }

  @SuppressLint("SetTextI18n")
  fun setJobHash(hash: Int) {
    tickerBinding.jobHash.text = "0x" + hash.toString(16).padStart(8, '0').toUpperCase(Locale.ROOT)
  }
}