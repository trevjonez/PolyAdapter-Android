package polyadapter.sample

import android.os.Bundle
import android.util.Log
import androidx.annotation.Keep
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import io.reactivex.disposables.CompositeDisposable
import polyadapter.ListProvider
import polyadapter.PolyAdapter
import polyadapter.provider.PagedListProvider
import polyadapter.provider.diffUtil
import polyadapter.sample.data.CategoryTitle
import polyadapter.sample.data.DividerLine
import polyadapter.sample.data.Movie
import polyadapter.sample.databinding.SampleActivityBinding
import polyadapter.sample.delegates.CategoryDelegate
import polyadapter.sample.delegates.DividerDelegate
import polyadapter.sample.delegates.MovieDelegate
import javax.inject.Inject

class SampleActivity : DaggerAppCompatActivity() {

  private lateinit var viewBinding: SampleActivityBinding

  @Inject
  lateinit var archThing: ArchitecturalThing

  @Inject
  lateinit var polyAdapter: PolyAdapter

  private val listProvider = ListProvider()

  private val createDisposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = DataBindingUtil.setContentView(this, R.layout.sample_activity)

    viewBinding.recycler.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = polyAdapter
    }

    archThing.dataSource() //grab your data source
        .diffUtil(listProvider) //pipe it into the list provider to calculate diff result
        .subscribe { it() } //apply the new list and diff result when you are ready
        .also { createDisposables.add(it) }
  }

  override fun onDestroy() {
    createDisposables.dispose()
    super.onDestroy()
  }

  @Module
  abstract class DelegatesModule {
    @Binds
    @IntoMap
    @ClassKey(CategoryTitle::class)
    abstract fun categoryDelegate(impl: CategoryDelegate):
        PolyAdapter.BindingDelegate<*, *>

    @Binds
    @IntoMap
    @ClassKey(DividerLine::class)
    abstract fun dividerDelegate(impl: DividerDelegate):
        PolyAdapter.BindingDelegate<*, *>

    @Binds
    @IntoMap
    @ClassKey(Movie::class)
    abstract fun movieDelegate(impl: MovieDelegate):
        PolyAdapter.BindingDelegate<*, *>
  }

  @Module
  object ProvidesModule {
    @Provides
    @JvmStatic
    fun itemProvider(activity: SampleActivity): PolyAdapter.ItemProvider = activity.listProvider
  }

  @Module
  abstract class BindingModule {
    @ContributesAndroidInjector(modules = [
      DelegatesModule::class,
      ProvidesModule::class
    ])
    abstract fun contributeInjector(): SampleActivity
  }
}

