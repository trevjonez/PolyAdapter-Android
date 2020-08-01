package polyadapter.sample

import android.os.Looper.getMainLooper
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.reactivex.internal.schedulers.ComputationScheduler
import io.reactivex.plugins.RxJavaPlugins
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.LooperMode.Mode.PAUSED
import polyadapter.AndroidLogsRule
import polyadapter.PauseableScheduler
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@LooperMode(PAUSED)
@Config(sdk = [28])
@RunWith(AndroidJUnit4::class)
class SampleActivityTest {

  @get:Rule
  val logRule = AndroidLogsRule()

  private val pausingCompScheduler = PauseableScheduler(ComputationScheduler())

  @Before
  fun setUp() {
    RxJavaPlugins.setComputationSchedulerHandler { pausingCompScheduler }
  }

  @After
  fun tearDown() {
    RxJavaPlugins.reset()
  }

  @Test
  fun `Sample items applied successfully`() {
    ActivityScenario.launch(SampleActivity::class.java).use {
      pausingCompScheduler.idlingResource.waitForIdle()

      onView(withId(R.id.recycler))
        .check(adapterItemCount(100))
    }
  }

  private tailrec fun CountingIdlingResource.waitForIdle(sleepMillis: Long = 20, timeoutMillis: Long = 1000, sleepTotal: Long = 0) {
    require(sleepMillis < timeoutMillis)
    shadowOf(getMainLooper()).idleFor(sleepMillis, TimeUnit.MILLISECONDS)
    Thread.sleep(sleepMillis)
    shadowOf(getMainLooper()).idleFor(sleepMillis, TimeUnit.MILLISECONDS)

    if (isIdleNow) return

    if (sleepTotal >= timeoutMillis - sleepMillis) {
      dumpStateToLogs()
      throw TimeoutException("IdlingResource \"$name\" failed to idle after ${sleepTotal + sleepMillis}ms")
    }

    waitForIdle(sleepMillis, timeoutMillis, sleepTotal + sleepMillis)
  }

  private fun adapterItemCount(expected: Int) = ViewAssertion { view, noViewFoundException ->
    noViewFoundException?.let { throw it }
    val recycler = view as RecyclerView
    val adapter = requireNotNull(recycler.adapter)

    assertThat("Wrong adapter item count", adapter.itemCount, Matchers.`is`(expected))
  }
}
