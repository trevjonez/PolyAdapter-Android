package polyadapter

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext

@ExperimentalCoroutinesApi
fun Flow<List<Any>>.diffUtil(
  listProvider: ListProvider,
  detectMoves: Boolean = true,
  dispatcher: CoroutineDispatcher = Dispatchers.Default
): Flow<ApplyDiffResult> = diffUtil { newList ->
  val diffWork = listProvider.updateItems(newList, detectMoves)
  suspend { withContext(dispatcher) { diffWork() } }
}

@ExperimentalCoroutinesApi
inline fun <T : Any> Flow<T>.diffUtil(
  crossinline diffWorkFactory: SuspendingDiffWorkFactory<T>
): Flow<ApplyDiffResult> {
  return transformLatest { emit(diffWorkFactory(it)()) }
}