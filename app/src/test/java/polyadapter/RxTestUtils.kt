package polyadapter

import androidx.test.espresso.idling.CountingIdlingResource
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.TimeUnit


/**
 * Allow wrapping a schedule with the ability to pause execution of work items
 * All pending work items will be scheduled when calling resume.
 * Once pending work items are scheduled calling pause will have no effect on them.
 */
class PauseableScheduler(private val actual: Scheduler = Schedulers.newThread()) : Scheduler() {

  @Volatile
  private var paused = false

  private val pendingWork = ConcurrentLinkedDeque<() -> Unit>()
  val idlingResource: CountingIdlingResource =
    CountingIdlingResource(PauseableScheduler::class.java.simpleName, true)

  fun pause() {
    paused = true
  }

  fun resume() {
    val pending = pendingWork.toList()
    pendingWork.clear()
    pending.forEach { it() }
    paused = false
  }

  override fun createWorker(): Worker {
    val realWorker = actual.createWorker()
    return object : Worker() {
      override fun dispose() = realWorker.dispose()
      override fun isDisposed() = realWorker.isDisposed

      override fun schedule(run: Runnable, delay: Long, unit: TimeUnit): Disposable {
        return CompositeDisposable().also { result ->

          val workItem: () -> Unit = {
            result.add(realWorker.schedule({
              try{

              idlingResource.increment()
              run.run()
              } finally {
                idlingResource.decrement()
              }
            }, delay, unit))
          }

          if (paused) {
            result.add(Disposables.fromRunnable {
              pendingWork.remove(workItem)
            })
            pendingWork.add(workItem)
          } else {
            workItem()
          }
        }
      }
    }
  }
}
