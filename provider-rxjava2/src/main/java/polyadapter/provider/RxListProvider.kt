package polyadapter.provider

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import polyadapter.PolyAdapter
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.android.MainThreadDisposable.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * RxJava based diff util management.
 * Transforms the new list into an effect function that when called,
 * dispatches the updated data set and diff result to the attached list update callback.
 */
class RxListProvider(
    defaultItems: List<Any> = emptyList(),
    private val workScheduler: Scheduler = Schedulers.computation(),
    private val mainScheduler: Scheduler = AndroidSchedulers.mainThread()
) : PolyAdapter.ItemProvider, ObservableTransformer<List<Any>, () -> Unit> {

  private lateinit var listUpdateCallback: ListUpdateCallback
  private lateinit var itemCallback: DiffUtil.ItemCallback<Any>
  private var list: List<Any> = defaultItems

  override fun getItem(position: Int) = list[position]
  override fun getItemCount() = list.size
  override fun onAttach(listUpdateCallback: ListUpdateCallback, itemCallback: DiffUtil.ItemCallback<Any>) {
    this.listUpdateCallback = listUpdateCallback
    this.itemCallback = itemCallback
  }

  override fun apply(upstream: Observable<List<Any>>): ObservableSource<() -> Unit> {
    return upstream.switchMap { newList ->
      val oldList = list
      Observable.fromCallable {
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
          override fun getOldListSize() = oldList.size

          override fun getNewListSize() = newList.size

          override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
              itemCallback.areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])

          override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
              itemCallback.areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])

          override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int) =
              itemCallback.getChangePayload(oldList[oldItemPosition], newList[newItemPosition])
        }, true) to newList
      }
          .map { (diffResult, newList) ->
            {
              verifyMainThread()
              list = newList
              diffResult.dispatchUpdatesTo(listUpdateCallback)
            }
          }
          .subscribeOn(workScheduler)
          .observeOn(mainScheduler)
    }
  }
}