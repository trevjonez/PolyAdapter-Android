package polyadapter


typealias ApplyDiffResult = () -> Unit

typealias DiffWork = () -> ApplyDiffResult

typealias DiffWorkFactory<T> = (newItems: T) -> DiffWork

typealias SuspendingDiffWork = suspend () -> ApplyDiffResult

typealias SuspendingDiffWorkFactory<T> = (newItems: T) -> SuspendingDiffWork