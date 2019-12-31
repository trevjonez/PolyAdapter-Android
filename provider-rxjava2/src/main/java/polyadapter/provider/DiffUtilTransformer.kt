package polyadapter.provider

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import polyadapter.ListProvider

/**
 * Transforms the new list into an effect function that when called,
 * dispatches the updated data set and diff result to the attached list update callback.
 *
 * In the event of a new list emission before the last diff finishes calculating,
 * the previous work request will be disposed preventing the dispatch function from being emitted.
 */
class DiffUtilTransformer<T : Any>(
  private val diffWorkFactory: (newData: T) -> () -> (() -> Unit),
  private val workScheduler: Scheduler = Schedulers.computation(),
  private val mainScheduler: Scheduler = AndroidSchedulers.mainThread()
) : ObservableTransformer<T, () -> Unit> {

  override fun apply(upstream: Observable<T>): ObservableSource<() -> Unit> {
    return upstream.switchMap { newList ->
      val diffWork = diffWorkFactory(newList)
      Observable.fromCallable { diffWork() }
        .subscribeOn(workScheduler)
        .observeOn(mainScheduler)
    }
  }
}

fun Observable<List<Any>>.diffUtil(listProvider: ListProvider): Observable<() -> Unit> =
  this.compose(DiffUtilTransformer(listProvider::updateItems))

@Deprecated(
  message = "Replaced by DiffUtilTransformer wrapping a ListProvider via `Observable.diffUtil(ListProvider)`",
  replaceWith = ReplaceWith("ListProvider()", "polyadapter.ListProvider")
)
@Suppress("FunctionName")
fun RxListProvider(
  defaultItems: List<Any> = emptyList(),
  workScheduler: Scheduler = Schedulers.computation(),
  mainScheduler: Scheduler = AndroidSchedulers.mainThread(),
  listProvider: ListProvider = ListProvider(defaultItems)
): Nothing = throw UnsupportedOperationException()
