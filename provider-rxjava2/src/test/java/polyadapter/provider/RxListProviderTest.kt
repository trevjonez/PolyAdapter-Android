package polyadapter.provider

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.google.common.truth.Truth.assertThat
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Test

class RxListProviderTest {
  @Test
  fun `diff calls delegated to attached item callback`() {
    val provider = RxListProvider(listOf(1, 2, 3), Schedulers.trampoline(), Schedulers.trampoline())
    val listCallback = ListCallbackFake()
    val itemCallback = ItemCallbackFake()
    provider.onAttach(listCallback, itemCallback)

    Observable.just(listOf(1, 2, 3))
        .compose(provider)
        .blockingFirst()

    assertThat(itemCallback.itemSameInvocations).hasSize(6)
    assertThat(itemCallback.contentsSameInvocations).hasSize(3)
    assertThat(itemCallback.changePayloadInvocations).isEmpty()
  }

  @Test
  fun `diff results delegated to attached list update callback`() {
    val provider = RxListProvider(listOf(1, 2, 3), Schedulers.trampoline(), Schedulers.trampoline())
    val listCallback = ListCallbackFake()
    val itemCallback = ItemCallbackFake()
    provider.onAttach(listCallback, itemCallback)

    Observable.just(listOf(3, 2, 1))
        .compose(provider)
        .blockingFirst()
        .invoke()

    assertThat(listCallback.movedInvocations).hasSize(2)
  }

  @Test
  fun `diff results and data not applied until effector is invoked`() {
    val provider = RxListProvider(listOf(1, 2, 3), Schedulers.trampoline(), Schedulers.trampoline())
    val listCallback = ListCallbackFake()
    val itemCallback = ItemCallbackFake()
    provider.onAttach(listCallback, itemCallback)

    val effector = Observable.just(listOf(3, 2, 1))
        .compose(provider)
        .blockingFirst()

    assertThat(listCallback.totalInvocations).isEqualTo(0)
    effector()
    assertThat(listCallback.totalInvocations).isGreaterThan(0)
  }
}

class ItemCallbackFake : DiffUtil.ItemCallback<Any>() {
  val itemSameInvocations = mutableListOf<Pair<Any, Any>>()
  override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
    itemSameInvocations.add(oldItem to newItem)
    return oldItem === newItem
  }

  val contentsSameInvocations = mutableListOf<Pair<Any, Any>>()
  override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
    contentsSameInvocations.add(oldItem to newItem)
    return oldItem == newItem
  }

  val changePayloadInvocations = mutableListOf<Pair<Any, Any>>()
  override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
    changePayloadInvocations.add(oldItem to newItem)
    return super.getChangePayload(oldItem, newItem)
  }
}

class ListCallbackFake : ListUpdateCallback {
  val totalInvocations: Int
    get() = changedInvocations.size +
        movedInvocations.size +
        insertedInvocations.size +
        removedInvocations.size

  val changedInvocations = mutableListOf<Triple<Int, Int, Any?>>()
  override fun onChanged(position: Int, count: Int, payload: Any?) {
    changedInvocations.add(Triple(position, count, payload))
  }

  val movedInvocations = mutableListOf<Pair<Int, Int>>()
  override fun onMoved(fromPosition: Int, toPosition: Int) {
    movedInvocations.add(fromPosition to toPosition)
  }

  val insertedInvocations = mutableListOf<Pair<Int, Int>>()
  override fun onInserted(position: Int, count: Int) {
    insertedInvocations.add(position to count)
  }

  val removedInvocations = mutableListOf<Pair<Int, Int>>()
  override fun onRemoved(position: Int, count: Int) {
    removedInvocations.add(position to count)
  }
}