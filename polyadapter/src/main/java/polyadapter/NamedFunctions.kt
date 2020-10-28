package polyadapter

/**
 * Function to swap the backing data and apply the diff util result.
 * By returning a function that represents this act, follow up actions be taken in a known state.
 * IE: Scroll to position, with confidence the item you expect at that position is available.
 */
fun interface ApplyDiffResult{
  operator fun invoke()
}

/**
 * Function representing a call to [androidx.recyclerview.widget.DiffUtil.calculateDiff]
 * That is also a factory of the action that applies the result.
 */
fun interface DiffWork {
  fun run(): ApplyDiffResult
}

fun interface DiffWorkFactory {
  fun create(newItems: List<Any>): DiffWork
}

interface SuspendingDiffWork {
  suspend fun run(): ApplyDiffResult
}

fun interface SuspendingDiffWorkFactory {
  fun create(newItems: List<Any>): SuspendingDiffWork
}