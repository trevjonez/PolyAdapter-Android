# PolyAdapter-Android

[ ![Download](https://api.bintray.com/packages/trevorjones141/maven/PolyAdapter-Android/images/download.svg) ](https://bintray.com/trevorjones141/maven/PolyAdapter-Android/_latestVersion)

A dagger centric recycler view adapter.

## Installation

Available via [jcenter](https://bintray.com/trevorjones141/maven/PolyAdapter-Android)

from jcenter: 
```groovy
implementation 'com.trevjonez.polyadapter:polyadapter:$polyAdapterVersion'
implementation 'com.trevjonez.polyadapter:provider-rxjava2:$polyAdapterVersion'
```

## Usage

To use the library are three core types you need to be aware of.

0. `PolyAdapter`
1. `PolyAdapter.ItemProvider`
2. `PolyAdapter.BindingDelegate`

The recommended way to consume `PolyAdapter` is via [dagger.](https://google.github.io/dagger/)

Provide a binding for `ItemProvider` and utilize `Map` multi-bindings for `Delegates`:
```kotlin
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

object ProvidesModule {
  @Provides
  fun itemProvider(activity: SampleActivity): 
      PolyAdapter.ItemProvider = activity.itemProvider
}
```

Request an injection of a `PolyAdapter` instance and pass it to your recycler:
```kotlin
val itemProvider = ListProvider()

@Inject lateinit var polyAdapter: PolyAdapter

[...]

recyclerView.apply {
  adapter = polyAdapter
}

```

To update the list contents send updates via the `ItemProvider` implementation.

`ListProvider`:
```kotlin
val itemProvider = ListProvider()

[...]

val diffWork = itemProvider.updateItems(newItems)

val applyNewData = diffWork() //do this from background thread

applyNewData() //do this on main-thread
```

Provided helpers make RX composition simple:
```kotlin
@Inject lateinit var itemProvider: ListProvider

[...]

someLiveDataSource
  .diffUtil(itemProvider)
  .subscribe { applyNewData ->
    applyNewData()
  }
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

Factory functions for common comparison patterns are provided:
```kotlin
override val itemCallback = equalityItemCallback<String>()
```
or if you want to customize the identity check:
```kotlin
override val itemCallback = equalityItemCallback<String> { hashCode() }
```
but if your usecase demands more fine grain control, provide a completely custom implementation of [`DiffUtil.ItemCallback`](https://developer.android.com/reference/android/support/v7/util/DiffUtil.ItemCallback).


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

  override val itemCallback = equalityItemCallback<String>()

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

#### Without using multibindings?

While the dynamics of dagger multi-bindings can be great, sometimes it just doesn't fit the use case well. 

To cover this, there is a secondary constructor on `PolyAdapter` that accepts a list of pre-built delegates.

```kotlin
val adapter = PolyAdapter(itemProvider, listOf(DelegateFoo(), DelegateBar()))
```

#### I want to use it without having to add an itemProvider directly to my dagger graph

In this case an `AssistedFactory` is provided so that dagger can deal with the multi bindings and your
code can manage the ItemProvider(s).

Not wanting to expose the item provider often fits when you have multiple item providers for a given scope:
```kotlin
@Inject
lateinit var adapterFactory: PolyAdapter.AssistedFactory

val itemProviderOne = ListProvider()
val itemProviderTwo = ListProvider()

[...]

recyclerViewOne.apply {
  adapter = adapterFactory.build(itemProviderOne)
}

recyclerViewTwo.apply {
  adapter = adapterFactory.build(itemProviderTwo)
}
```

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

## Notes on the future

I still think the main thing google got wrong with recycle view is that they allowed things to become too fuzzy, optional, and/or coupled. Loading items and binding data to views should not have been put inside a single type. This library I have always seen as a way to force implementations to acqknowledge the elements of the recycler view API's to make the system work as I think it was intended. Load -> diff -> incrementally bind. I started working on a coroutines version of the API's some time ago as I was retraining my brain to think in `suspend fun` rather than `Subscribe`/`Disposable`. That said I don't know how widely this library is used in industry. The way compose exposes the option of setting keys on lazy lists/columns I believe implicitly captures the load/diff/bind pattern that was arduous before and is now mostly compiler intrinsic (how we can assume it is the right way to go forward?). 

I am willing to support community needs on the project as time allows, but I suspect the need for it will be brief and rapidly diminishing.
