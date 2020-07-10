package polyadapter

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext

suspend fun ListProvider.calculateDiff(
  newList: List<Any>,
  detectMoves: Boolean = true,
  dispatcher: CoroutineDispatcher = Dispatchers.Default
): ApplyDiffResult {
  val diffWork = updateItems(newList, detectMoves)
  return withContext(dispatcher) { diffWork() }
}

@ExperimentalCoroutinesApi
fun Flow<List<Any>>.diffUtil(
  listProvider: ListProvider,
  detectMoves: Boolean = true,
  dispatcher: CoroutineDispatcher = Dispatchers.Default
): Flow<ApplyDiffResult> = diffUtil { newList ->
  suspend { listProvider.calculateDiff(newList, detectMoves, dispatcher) }
}

@ExperimentalCoroutinesApi
inline fun Flow<List<Any>>.diffUtil(
  crossinline diffWorkFactory: SuspendingDiffWorkFactory
): Flow<ApplyDiffResult> {
  return transformLatest { emit(diffWorkFactory(it)()) }
}