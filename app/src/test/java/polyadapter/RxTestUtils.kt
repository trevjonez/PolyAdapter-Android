package polyadapter

import androidx.test.espresso.idling.CountingIdlingResource
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Allow wrapping a schedule with the ability to pause execution of work items
 * All pending work items will be scheduled when calling resume.
 * Once pending work items are scheduled calling pause will have no effect on them.
 */
class PauseableScheduler(private val actual: Scheduler = Schedulers.newThread()) : Scheduler() {

  @Volatile
  private var paused = false

  private val pendingWork = LinkedList<() -> Unit>()
  val idlingResource: CountingIdlingResource =
    CountingIdlingResource(PauseableScheduler::class.java.simpleName, true)

  fun pause() {
    synchronized(pendingWork) {
      paused = true
    }
  }

  fun resume() {
    synchronized(pendingWork) {
      val pending = pendingWork.toList()
      pendingWork.clear()
      pending.forEach { it() }
      paused = false
    }
  }

  override fun createWorker(): Worker {
    val realWorker = actual.createWorker()
    return object : Worker() {
      override fun dispose() = realWorker.dispose()
      override fun isDisposed() = realWorker.isDisposed

      init {
        idlingResource.increment()
      }

      override fun schedule(run: Runnable, delay: Long, unit: TimeUnit): Disposable {
        return synchronized(pendingWork) {
          CompositeDisposable().also { result ->
            val decrementingRun = Runnable {
              run.run()
              idlingResource.decrement()
            }
            val workItem: () -> Unit = {
              realWorker.schedule(decrementingRun, delay, unit).also { result.add(it) }
            }

            if (paused) {
              Disposables.fromRunnable { pendingWork.remove(workItem) }.also { result.add(it) }
              pendingWork.add(workItem)
            } else {
              workItem()
            }
          }
        }
      }
    }
  }
}
