@file:JvmName("PolyAdapter")

package com.trevjonez.polyadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.collection.SimpleArrayMap
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView

class PolyAdapter(val itemProvider: ItemProvider) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  private val layoutIdRegistry = SimpleArrayMap<Int, BindingDelegate<*, *>>()
  private val classTypeRegistry = SimpleArrayMap<Class<*>, BindingDelegate<*, *>>()
  private val itemCallback: DiffUtil.ItemCallback<Any> = PolyAdapterItemCallback()

  init {
    itemProvider.onAttach(AdapterListUpdateCallback(this), itemCallback)
  }

  /**
   * How [PolyAdapter] gets it's items, from a
   * [List], [androidx.paging.PagedList],
   * or whatever you want to implement.
   */
  interface ItemProvider {
    fun getItemCount(): Int
    fun getItem(position: Int): Any

    /**
     * Always called exactly once from the [PolyAdapter] init block.
     *
     * Allows the ItemProvider to capture a reference for change notifications
     * without introducing a compile time cyclic dependency.
     */
    fun onAttach(listUpdateCallback: ListUpdateCallback, itemCallback: DiffUtil.ItemCallback<Any>)
  }

  /**
   * The bare minimum properties and methods to describe the data type and view relationship.
   */
  interface BindingDelegate<ItemType, HolderType : RecyclerView.ViewHolder> {
    @get:LayoutRes
    val layoutId: Int
    val dataType: Class<ItemType>
    val itemCallback: DiffUtil.ItemCallback<ItemType>
    fun createViewHolder(itemView: View): HolderType
    fun bindView(holder: HolderType, item: ItemType)
  }

  /**
   * Implement on an instance of [BindingDelegate] to receive incremental bindView callbacks
   */
  interface IncrementalBindingDelegate<in ItemType, HolderType : RecyclerView.ViewHolder> {
    fun bindView(holderType: HolderType, item: ItemType, payloads: List<Any>)
  }

  /**
   * Implement on an instance of [BindingDelegate] to receive onViewRecycled callbacks
   */
  interface OnViewRecycledDelegate<in HolderType : RecyclerView.ViewHolder> {
    fun onRecycle(holder: HolderType)
  }

  /**
   * Implement on an instance of [BindingDelegate] to receive onViewAttached callbacks
   */
  interface OnViewAttachedDelegate<in HolderType : RecyclerView.ViewHolder> {
    fun onAttach(holder: HolderType)
  }

  /**
   * Implement on an instance of [BindingDelegate] to receive onViewDetached callbacks
   */
  interface OnViewDetachedDelegate<in HolderType : RecyclerView.ViewHolder> {
    fun onDetach(holder: HolderType)
  }

  private fun getItem(position: Int) = itemProvider.getItem(position)

  override fun getItemCount() = itemProvider.getItemCount()

  /**
   * Add a new delegate to this [PolyAdapter]'s lookup tables.
   */
  fun addDelegate(delegate: BindingDelegate<*, *>) {
    require(itemCount == 0) { "Do not modify delegate listing after supplying data to the ItemProvider" }
    val viewTypeOverwrite = layoutIdRegistry.put(delegate.layoutId, delegate)
    val dataTypeOverwrite = classTypeRegistry.put(delegate.dataType, delegate)

    when {
      viewTypeOverwrite != null && dataTypeOverwrite == null ->
        throw IllegalArgumentException(
            "Partial delegate overwrite.\n" +
                "Layout id: '${delegate.layoutId}' collides between '$viewTypeOverwrite' and '$delegate'.\n" +
                "You can use a resource alias to disambiguate multiple data types using the same layout.\n" +
                "`<item name=\"the_alias_name\" type=\"layout\">@layout/the_real_name</item>`"
        )

      viewTypeOverwrite == null && dataTypeOverwrite != null ->
        throw IllegalArgumentException(
            "Partial delegate overwrite.\n" +
                "Data type: '${delegate.dataType}' collides between '$dataTypeOverwrite' and '$delegate'."
        )

      viewTypeOverwrite != null && dataTypeOverwrite != null ->
        throw IllegalArgumentException(
            "Total delegate overwrite.\n" +
                "Layout id: '${delegate.layoutId}' and data type: '${delegate.dataType}' collides between '$viewTypeOverwrite' and '$delegate'."
        )
    }
  }

  override fun getItemViewType(position: Int): Int {
    val item = getItem(position)
    return requireNotNull(classTypeRegistry[item.javaClass]) {
      "Failed to get layout id for item of type ${item.javaClass.name}"
    }.layoutId
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val delegate = requireNotNull(layoutIdRegistry[viewType]) {
      "Not binding delegate found for viewType $viewType." +
          "This should never happen as the delegate provides the viewType view the layout id property"
    }
    val inflater = LayoutInflater.from(parent.context)
    return delegate.createViewHolder(inflater.inflate(delegate.layoutId, parent, false))
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val item = getItem(position)
    getDelegate(item.javaClass).bindView(holder, item)
  }

  override fun onBindViewHolder(
      holder: RecyclerView.ViewHolder,
      position: Int,
      payloads: List<Any>
  ) {
    val item = getItem(position)
    val delegate = getDelegate(item.javaClass)
    val incrementalDelegate = delegate.asIncremental()

    when {
      payloads.isNotEmpty() && incrementalDelegate != null -> {
        incrementalDelegate.bindView(holder, item, payloads)
      }
      else -> delegate.bindView(holder, item)
    }
  }

  override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
    getDelegate(holder.itemViewType).asViewRecycledDelegate()?.onRecycle(holder)
  }

  override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
    getDelegate(holder.itemViewType).asViewAttachedDelegate()?.onAttach(holder)
  }

  override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
    getDelegate(holder.itemViewType).asViewDetachedDelegate()?.onDetach(holder)
  }

  inner class PolyAdapterItemCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
      return when {
        oldItem.javaClass == newItem.javaClass -> {
          getDelegate(newItem.javaClass).itemCallback.areItemsTheSame(oldItem, newItem)
        }
        else -> false
      }
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
      return when {
        oldItem.javaClass == newItem.javaClass -> {
          getDelegate(newItem.javaClass).itemCallback.areContentsTheSame(oldItem, newItem)
        }
        else -> false
      }
    }

    override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
      return getDelegate(newItem.javaClass).itemCallback.getChangePayload(oldItem, newItem)
    }
  }

  private fun getDelegate(layoutId: Int): BindingDelegate<Any, RecyclerView.ViewHolder> {
    @Suppress("UNCHECKED_CAST")
    return layoutIdRegistry[layoutId] as BindingDelegate<Any, RecyclerView.ViewHolder>
  }

  private fun getDelegate(clazz: Class<*>): BindingDelegate<Any, RecyclerView.ViewHolder> {
    @Suppress("UNCHECKED_CAST")
    return classTypeRegistry[clazz] as BindingDelegate<Any, RecyclerView.ViewHolder>
  }

  private inline fun <reified T> BindingDelegate<Any, RecyclerView.ViewHolder>.asType(): T? {
    return this as? T
  }

  private fun BindingDelegate<Any, RecyclerView.ViewHolder>.asIncremental() =
      asType<IncrementalBindingDelegate<Any, RecyclerView.ViewHolder>>()

  private fun BindingDelegate<Any, RecyclerView.ViewHolder>.asViewRecycledDelegate() =
      asType<OnViewRecycledDelegate<RecyclerView.ViewHolder>>()

  private fun BindingDelegate<Any, RecyclerView.ViewHolder>.asViewAttachedDelegate() =
      asType<OnViewAttachedDelegate<RecyclerView.ViewHolder>>()

  private fun BindingDelegate<Any, RecyclerView.ViewHolder>.asViewDetachedDelegate() =
      asType<OnViewDetachedDelegate<RecyclerView.ViewHolder>>()

  /**
   * Blindly cast the itemProvider to [T]
   */
  inline fun <reified T : PolyAdapter.ItemProvider> provider(): T {
    return itemProvider as? T
        ?: throw UnsupportedOperationException(
            "itemProvider was type ${itemProvider.javaClass.simpleName} " +
                "but expected ${T::class.java.simpleName}"
        )
  }
}
