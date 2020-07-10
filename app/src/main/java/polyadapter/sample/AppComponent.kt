package polyadapter.sample

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(
  modules = [
    SampleActivity.ContribModule::class,
    AndroidInjectionModule::class
  ]
)
interface AppComponent : AndroidInjector<App>
