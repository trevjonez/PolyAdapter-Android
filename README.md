# PolyAdapter-Android

[ ![Download](https://api.bintray.com/packages/trevorjones141/maven/PolyAdapter-Android/images/download.svg?version=0.1.0) ](https://bintray.com/trevorjones141/maven/PolyAdapter-Android/0.1.0/link)

Yet another recycler view adapter library that claims to be the last you will ever need.

## Installation

Available via jcenter(pending still):

```groovy
implementation 'com.trevjonez.polyadapter:core:0.1.0'
```

## Usage

To use the library there is basically three types you need to be aware of. 

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

Lets assume for a moment you are a world class Android engineer and need
more methods from `RecyclerView.Adapter` that don't exist on `PolyAdapter.BindingDelegate`

We thought of that and added a few optional interfaces for you to implement
on your delegate classes if you care to receive those callbacks.

They are as follows:

`PolyAdapter.IncrementalBindingDelegate` - Adds an additional `bindView`
method that includes payloads that are returned from your `DiffUtil.ItemCallback#getChangePayload`

`PolyAdapter.OnViewRecycledDelegate` - Adds `onRecycle`

`PolyAdapter.OnViewAttachedDelegate` - Adds `onAttach`

`PolyAdapter.OnViewDetachedDelegate` - Adds `onDetach`

## But why?

Most existing libraries are pretty good, and work well enough,
but still fall short on one or more of the [SOLID principles of OOP.](https://en.wikipedia.org/wiki/SOLID)
Most of the other solutions lack inversion of control, or require you
subclass an abstract class losing composability options as a result.

At the end of the day it turns out that I just have strong opinions
about how recycler views should be setup. This implementation allows for
very nice composition and pluggable list setups through things like dagger2
 multi bindings. You can have your list items actually include
a `ViewModel`, `Presenter`, `Reducer` or whatever else so that the bind
method actually does just that, binds your flavor of the week architecture
to the view.

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