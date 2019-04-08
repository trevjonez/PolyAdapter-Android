package polyadapter.sample

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.*
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import polyadapter.PauseableScheduler
import polyadapter.ShampooRule
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
class SampleActivityTest {
  private val pausingCompScheduler = PauseableScheduler()

  @Before
  fun setUp() {
    IdlingRegistry.getInstance().register(pausingCompScheduler.idlingResource)
    RxJavaPlugins.setComputationSchedulerHandler { pausingCompScheduler }
  }

  @After
  fun tearDown() {
    IdlingRegistry.getInstance().unregister(pausingCompScheduler.idlingResource)
    RxJavaPlugins.reset()
  }

  @Test
  fun `Default empty list first then items applied successfully`() {
    pausingCompScheduler.pause()
    ActivityScenario.launch(SampleActivity::class.java).use {
      onView(withId(R.id.recycler))
          .check(adapterItemCount(0))

      pausingCompScheduler.resume()
      pausingCompScheduler.idlingResource.waitForIdle()

      onView(withId(R.id.recycler))
          .check(adapterItemCount(13))
    }
  }

  private fun IdlingResource.waitForIdle(sleepMillis: Long = 10, timeoutMillis: Long = 2000) {
    var sleepTime = 0L
    while (!isIdleNow) {
      Thread.sleep(sleepMillis)
      sleepTime += sleepMillis
      if (sleepTime >= timeoutMillis)
        throw TimeoutException("IdlingResource \"$name\" failed to idle after ${sleepTime}ms")
    }
  }

  private fun adapterItemCount(expected: Int) = ViewAssertion { view, noViewFoundException ->
    noViewFoundException?.let { throw it }
    val recycler = view as RecyclerView
    val adapter = requireNotNull(recycler.adapter)

    assertThat("Wrong adapter item count", adapter.itemCount, Matchers.`is`(expected))
  }
}