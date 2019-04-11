# PolyAdapter-Android

[ ![Download](https://api.bintray.com/packages/trevorjones141/maven/PolyAdapter-Android/images/download.svg) ](https://bintray.com/trevorjones141/maven/PolyAdapter-Android/_latestVersion)

A composable recycler view adapter.

## Installation

Available via [jcenter](https://bintray.com/trevorjones141/maven/PolyAdapter-Android)

with the artifact renaming and addition in 0.6.0, the bintray to jcenter sync is broken. add `maven { url("https://dl.bintray.com/trevorjones141/maven") }` as a workaround until resolved.

from jcenter: 
```groovy
implementation 'com.trevjonez.polyadapter:core:$polyAdapterVersion'
implementation 'com.trevjonez.polyadapter:provider-rxjava2:$polyAdapterVersion'
```

## Usage

To use the library are three core types you need to be aware of.

0. `PolyAdapter`
1. `PolyAdapter.ItemProvider`
2. `PolyAdapter.BindingDelegate`

The recommended way to consume `PolyAdapter` is via [dagger.](https://google.github.io/dagger/)

Provide a binding for `ItemProvider` and utilize map multi-bindings for `Delegates`:
```kotlin
@Module(includes = [
  DelegatesModule::class,
  ProviderModule::class
])
abstract class PolyAdapterConfigModule

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

  @Binds
  abstract fun listProvider(impl: RxListProvider):
      PolyAdapter.ItemProvider
}

@Module
class ProviderModule {
  @Provides
  @ActivityScope
  fun rxProvider() = RxListProvider()
}
```

Request an injection of a `PolyAdapter` instance and pass it to your recycler:
```kotlin

@Inject lateinit var polyAdapter: PolyAdapter

[...]

recyclerView.apply {
  adapter = polyAdapter
}

```

To update the list contents send updates via the `ItemProvider` implementation.
`RxListProvider`:
```kotlin
@Inject lateinit var itemProvider: RxListProvider

[...]

someLiveDataSource
  .compose(itemProvider)
  .subscribe { applyNewData ->
    applyNewData()
  }
```

`ListProvider`:
```kotlin
@Inject lateinit var itemProvider: ListProvider

[...]

val diffWork = itemProvider.updateItems(newItems)

val applyNewData = diffWork() //do this from background thread

applyNewData() //do this on main thread
```

#### Binding Delegate's

Binding delegates are the core of what makes this library great. It is a
(bare minimum) set of methods that we use to describe to the adapter the
relationship between your data and view.

We start with creating our delegate class:
```kotlin
class SimpleTextItemDelegate: PolyAdapter.BindingDelegate<String, TextItemHolder> {
}
```

In order for the `PolyAdapter` to lookup the correct delegate for binding
we need to provide a few constants:
```kotlin
override val layoutId = R.layout.textItem
override val dataType = String::class.java
```

The pre-implemented item providers always use [`DiffUtil`](https://developer.android.com/reference/android/support/v7/util/DiffUtil)
to compare your data and notify the adapter of changes, so the next property
to implement is a [`DiffUtil.ItemCallback`](https://developer.android.com/reference/android/support/v7/util/DiffUtil.ItemCallback) for your data type.

```kotlin
override val itemCallback = object : DiffUtil.ItemCallback<String>() {
  override fun areItemsTheSame(oldItem: String?, newItem: String?) =
      oldItem === newItem

  override fun areContentsTheSame(oldItem: String?, newItem: String?) =
      oldItem == newItem
}
```

This leaves us with two methods left to implement:

```kotlin
override fun createViewHolder(itemView: View) = TextItemHolder(itemView)

override fun bindView(holder: TextItemHolder, item: String) {
  holder.setTitleText(item)
}
```


So all together we have the following:

```kotlin
class SimpleTextItemDelegate: PolyAdapter.BindingDelegate<String, TextItemHolder> {

  override val layoutId = R.layout.textItem

  override val dataType = String::class.java

  override val itemCallback = object : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String?, newItem: String?) =
        oldItem === newItem

    override fun areContentsTheSame(oldItem: String?, newItem: String?) =
        oldItem == newItem
  }

  override fun createViewHolder(itemView: View) = TextItemHolder(itemView)

  override fun bindView(holder: TextItemHolder, item: String) {
    holder.setTitleText(item)
  }
}
```

#### More Delegates

If you need more methods from `RecyclerView.Adapter` that don't exist on `PolyAdapter.BindingDelegate`

There are a few optional interfaces for you to implement on your delegate classes.

They are as follows:

`PolyAdapter.IncrementalBindingDelegate` - Adds an additional `bindView`
method that includes payloads that are returned from your `DiffUtil.ItemCallback#getChangePayload`

`PolyAdapter.OnViewRecycledDelegate` - Adds `onRecycle`

`PolyAdapter.OnViewAttachedDelegate` - Adds `onAttach`

`PolyAdapter.OnViewDetachedDelegate` - Adds `onDetach`

## License

    Copyright 2018 Trevor Jones

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
