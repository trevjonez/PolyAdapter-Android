package polyadapter


typealias ApplyDiffResult = () -> Unit

typealias DiffWork = () -> ApplyDiffResult

typealias DiffWorkFactory = (newItems: List<Any>) -> DiffWork

typealias SuspendingDiffWork = suspend () -> ApplyDiffResult

typealias SuspendingDiffWorkFactory = (newItems: List<Any>) -> SuspendingDiffWork