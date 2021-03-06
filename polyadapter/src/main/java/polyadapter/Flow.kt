package polyadapter

import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest
import java.util.*

suspend fun ListProvider.calculateDiff(
  newList: List<Any>,
  detectMoves: Boolean = true,
  dispatcher: CoroutineDispatcher = Dispatchers.Default
): ApplyDiffResult {
  val diffWork = updateItems(newList, detectMoves)
  return withContext(dispatcher) { diffWork.run() }
}

@ExperimentalCoroutinesApi
fun Flow<List<Any>>.diffUtil(
  listProvider: ListProvider,
  detectMoves: Boolean = true,
  dispatcher: CoroutineDispatcher = Dispatchers.Default
): Flow<ApplyDiffResult> = diffUtil { newList ->
  object : SuspendingDiffWork {
    override suspend fun run(): ApplyDiffResult {
      return listProvider.calculateDiff(newList, detectMoves, dispatcher)
    }
  }
}

@ExperimentalCoroutinesApi
fun Flow<List<Any>>.diffUtil(
  diffWorkFactory: SuspendingDiffWorkFactory
): Flow<ApplyDiffResult> = transformLatest { newList ->
  val diffResult = diffWorkFactory.create(newList).run()
  emit(diffResult)
}

/**
 * [PolyAdapter.BindingDelegate] that has its own scope as well as a scope per holder.
 */
interface ScopedDelegate : PolyAdapter.OnViewRecycledDelegate<RecyclerView.ViewHolder> {
  val coroutineScope: CoroutineScope

  val RecyclerView.ViewHolder.coroutineScope: CoroutineScope

  companion object {
    fun default(parentScope: CoroutineScope): ScopedDelegate =
      ScopedDelegateDefaultImpl(parentScope)
  }
}

private class ScopedDelegateDefaultImpl(
  private val parentScope: CoroutineScope
) : ScopedDelegate, CoroutineScope by parentScope + Job() {

  override val coroutineScope: CoroutineScope
    get() = this

  private val holderScopes = WeakHashMap<RecyclerView.ViewHolder, CoroutineScope>()

  override val RecyclerView.ViewHolder.coroutineScope: CoroutineScope
    get() = holderScopes.getOrPut(this) {
      this@ScopedDelegateDefaultImpl + Job()
    }

  override fun onRecycle(holder: RecyclerView.ViewHolder) {
    holder.coroutineScope.coroutineContext.cancelChildren()
  }
}
