# PolyAdapter-Android

[ ![Download](https://api.bintray.com/packages/trevorjones141/maven/PolyAdapter-Android/images/download.svg?version=0.3.0) ](https://bintray.com/trevorjones141/maven/PolyAdapter-Android/0.3.0/link) [![](https://jitpack.io/v/trevjonez/polyadapter-android.svg)](https://jitpack.io/#trevjonez/polyadapter-android)

A composable recycler view adapter.

## Installation

Available via [jcenter](https://bintray.com/trevorjones141/maven/PolyAdapter-Android) or [jitpack.io](https://jitpack.io/#trevjonez/polyadapter-android):

from jcenter: 
```groovy
//TODO: Jcenter didn't like variant aware publishing of 0.3.0 and fails to resolve.
implementation 'com.trevjonez.polyadapter:core:0.3.0'
```

from jitpack:
```groovy
 implementation 'com.github.trevjonez.polyadapter-android:core:0.3.0'
```

## Usage

To use the library are three core types you need to be aware of.

0. `PolyAdapter`
1. `PolyAdapter.ItemProvider`
2. `PolyAdapter.BindingDelegate`

In order to create a `PolyAdapter` you need to provide the constructor with a `PolyAdapter.ItemProvider` implementation.
You can use one of the provided ItemProvider implementations or create your own.

`PolyListItemProvider` operates on `List<Any>`:
```kotlin
val adapter = PolyAdapter(PolyListItemProvider())
```

Then you can add your `PolyAdapter.BindingDelegate` implementations to your new adapter:
```kotlin
val polyAdapter = PolyAdapter(PolyListItemProvider()).apply {
  addDelegate(FooDelegate())
  addDelegate(BarDelegate())
}

recyclerView.apply {
  layoutManager = LinearLayoutManager()
  adapter = polyAdapter
}
```

After that your adapter is ready to serve items via the ItemProvider we injected it with.
This can be done either directly through the item provider's concrete api or one of the
extension methods provided in the main library to make it feel a bit more familiar.

```kotlin
someLiveDataSource.subscribe { listItems ->
  adapter.updateList(listItems)
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
