package polyadapter.provider

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.google.common.truth.Truth.assertThat
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import polyadapter.ListProvider

class DiffUtilTransformerTest {

  private val workScheduler = CountingSchedulerWrapper(Schedulers.trampoline())
  private val mainScheduler = CountingSchedulerWrapper(Schedulers.trampoline())

  @Test
  fun `schedulers invoked as expected`() {
    val provider = ListProvider(listOf(1, 2, 3))
    val transformer = DiffUtilTransformer(provider::updateItems, workScheduler, mainScheduler)
    val listCallback = ListCallbackFake()
    val itemCallback = ItemCallbackFake()
    provider.onAttach(listCallback, itemCallback)

    assertThat(workScheduler.workersCreated).isEqualTo(0)
    assertThat(mainScheduler.workersCreated).isEqualTo(0)
    Observable.just(listOf(3, 2, 1))
      .compose(transformer)
      .blockingFirst()
    assertThat(workScheduler.workersCreated).isEqualTo(1)
    assertThat(mainScheduler.workersCreated).isEqualTo(1)
  }
}

class CountingSchedulerWrapper(private val actual: Scheduler) : Scheduler() {
  var workersCreated = 0L
  override fun createWorker(): Worker {
    workersCreated++
    return actual.createWorker()
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
