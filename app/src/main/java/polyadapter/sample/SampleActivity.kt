package polyadapter.sample

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import polyadapter.ListProvider
import polyadapter.PolyAdapter
import polyadapter.diffUtil
import polyadapter.provider.diffUtil
import polyadapter.sample.databinding.SampleActivityBinding
import javax.inject.Inject
import javax.inject.Scope

class SampleActivity : DaggerAppCompatActivity() {

  private lateinit var viewBinding: SampleActivityBinding

  @Inject
  lateinit var archThing: ArchitecturalThing

  @Inject
  lateinit var polyAdapter: PolyAdapter

  @Inject
  lateinit var polyAdapterFactory: PolyAdapter.AssistedFactory

  @Inject
  lateinit var listProvider : ListProvider

  private val createDisposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = SampleActivityBinding.inflate(layoutInflater)
    setContentView(viewBinding.root)

    viewBinding.recycler.adapter = polyAdapter
    // or
    viewBinding.recycler.adapter = polyAdapterFactory.build(listProvider)

    // rx data source
    archThing.rxDataSource() //grab your data source
      .diffUtil(listProvider) //pipe it into the list provider to calculate diff result
      .subscribe { it() } //apply the new list and diff result when you are ready
      .addTo(createDisposables)

    // or

    // coroutines flow data source
    archThing.flowDataSource() //grab your data source
      .diffUtil(listProvider) //pipe it into the list provider to calculate diff result
      .onEach { it() } //apply the new list and diff result when you are ready
      .launchIn(lifecycleScope)
  }

  override fun onDestroy() {
    createDisposables.dispose()
    super.onDestroy()
  }

  @Module(includes = [ListProvider.AsItemProvider::class])
  interface AdapterModule {

    companion object {

      @Provides
      @ActivityScope
      fun listProvider() = ListProvider()

      @Provides
      fun hostLifecycle(activity: SampleActivity) = activity.lifecycle
    }

    @Binds
    @IntoMap
    @ClassKey(CategoryTitle::class)
    fun CategoryDelegate.category(): PolyAdapter.BindingDelegate<*, *>

    @Binds
    @IntoMap
    @ClassKey(DividerLine::class)
    fun DividerDelegate.divider(): PolyAdapter.BindingDelegate<*, *>

    @Binds
    @IntoMap
    @ClassKey(Movie::class)
    fun MovieDelegate.movie(): PolyAdapter.BindingDelegate<*, *>

    @Binds
    @IntoMap
    @ClassKey(Ticker::class)
    fun TickerDelegate.ticker(): PolyAdapter.BindingDelegate<*, *>
  }

  @Module
  abstract class ContribModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [AdapterModule::class])
    abstract fun contributeInjector(): SampleActivity
  }
}

@Scope
annotation class ActivityScope