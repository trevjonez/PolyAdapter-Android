package polyadapter.provider

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import polyadapter.ApplyDiffResult
import polyadapter.DiffWorkFactory
import polyadapter.ListProvider

/**
 * Transforms the new list into an effect function that when called,
 * dispatches the updated data set and diff result to the attached list update callback.
 *
 * In the event of a new list emission before the last diff finishes calculating,
 * the previous work request will be disposed preventing the dispatch function from being emitted.
 */
class DiffUtilTransformer<T : Any>(
  private val diffWorkFactory: DiffWorkFactory<T>,
  private val workScheduler: Scheduler = Schedulers.computation(),
  private val mainScheduler: Scheduler = AndroidSchedulers.mainThread()
) : ObservableTransformer<T, ApplyDiffResult> {

  override fun apply(upstream: Observable<T>): ObservableSource<ApplyDiffResult> {
    return upstream.switchMap { newList ->
      val diffWork = diffWorkFactory(newList)
      Observable.fromCallable { diffWork() }
        .subscribeOn(workScheduler)
        .observeOn(mainScheduler)
    }
  }
}

fun Observable<List<Any>>.diffUtil(listProvider: ListProvider): Observable<ApplyDiffResult> =
  compose(DiffUtilTransformer({ listProvider.updateItems(it) }))

