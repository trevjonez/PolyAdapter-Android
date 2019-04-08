package polyadapter.provider

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import polyadapter.ListProvider
import polyadapter.PolyAdapter

/**
 * RxJava2 friendly diff util management.
 * Transforms the new list into an effect function that when called,
 * dispatches the updated data set and diff result to the attached list update callback.
 *
 * If new lists arrive before the last is done diff calculating the previous work request will be disposed.
 */
class RxListProvider(
    defaultItems: List<Any> = emptyList(),
    private val workScheduler: Scheduler = Schedulers.computation(),
    private val mainScheduler: Scheduler = AndroidSchedulers.mainThread(),
    private val listProvider: ListProvider = ListProvider(defaultItems)
) : PolyAdapter.ItemProvider by listProvider,
    ObservableTransformer<List<Any>, () -> Unit> {

  override fun apply(upstream: Observable<List<Any>>): ObservableSource<() -> Unit> {
    return upstream.switchMap { newList ->
      val diffWork = listProvider.updateItems(newList)
      Observable.fromCallable { diffWork() }
          .subscribeOn(workScheduler)
          .observeOn(mainScheduler)
    }
  }
}