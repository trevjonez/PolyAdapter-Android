package polyadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_ID
import javax.inject.Inject
import javax.inject.Provider

class PolyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private val typeLookup = mutableMapOf<Class<*>, BindingDelegate<*, *>>()
  private val layoutLookup = mutableMapOf<Int, BindingDelegate<*, *>>()

  private val itemProvider: ItemProvider
  private val delegateFactories: Map<Class<*>, @JvmSuppressWildcards Provider<BindingDelegate<*, *>>>

  @Inject
  constructor(
    itemProvider: ItemProvider,
    delegateFactories: Map<Class<*>,
      @JvmSuppressWildcards Provider<BindingDelegate<*, *>>>
  ) : super() {
    this.itemProvider = itemProvider
    this.delegateFactories = delegateFactories
    itemProvider.onAttach(AdapterListUpdateCallback(this), CompositeItemCallback())
  }

  /**
   * Optional manual use constructor for the times manual creation makes more sense.
   */
  constructor(
    itemProvider: ItemProvider,
    delegates: List<BindingDelegate<*, *>>
  ) : super() {
    this.itemProvider = itemProvider
    this.delegateFactories = emptyMap()
    synchronized(typeLookup) { delegates.forEach(::insertDelegate) }
    itemProvider.onAttach(AdapterListUpdateCallback(this), CompositeItemCallback())
  }

  /**
   * Optional assisted inject factory to simplify item provider instance management.
   */
  class AssistedFactory @Inject constructor(
    private val delegateFactories: Map<Class<*>, @JvmSuppressWildcards Provider<BindingDelegate<*, *>>>
  ) {

    fun build(itemProvider: ItemProvider): PolyAdapter = PolyAdapter(itemProvider, delegateFactories)
  }

  /**
   * [PolyAdapter] data owner, lookups are delegated to an instance of [ItemProvider]
   */
  interface ItemProvider {

    /**
     * See: [RecyclerView.Adapter.getItemCount]
     */
    fun getItemCount(): Int

    fun getItem(position: Int): Any

    /**
     * Always called exactly once from the [PolyAdapter] init block.
     *
     * Allows the ItemProvider to capture a reference for change notifications.
     */
    fun onAttach(listUpdateCallback: ListUpdateCallback, itemCallback: DiffUtil.ItemCallback<Any>)
  }

  /**
   * The minimum properties and methods to describe the adaptation of an [ItemType] to a [HolderType].
   *
   * Note: The default behavior of [PolyAdapter] is to lazily request the delegate via [Provider.get].
   * As a result, [BindingDelegate]'s are frequently created on a background thread when creation
   * is triggered by [DiffUtil] comparing an instance of [ItemType].
   */
  interface BindingDelegate<ItemType : Any, HolderType : RecyclerView.ViewHolder> {

    /**
     * Layout ID to be used to inflate the [RecyclerView.ViewHolder.itemView].
     * Must be unique for a given instance of [PolyAdapter].
     */
    @get:LayoutRes
    val layoutId: Int

    /**
     * [ItemType] literal. Used only for delegate lookup.
     * Must be unique for a given instance of [PolyAdapter].
     */
    val dataType: Class<ItemType>

    /**
     * Comparison strategy used within [CompositeItemCallback].
     */
    val itemCallback: DiffUtil.ItemCallback<ItemType>

    /**
     * Factory function for the given [HolderType].
     */
    fun createViewHolder(itemView: View): HolderType

    /**
     * See: [RecyclerView.Adapter.onBindViewHolder]
     */
    fun bindView(holder: HolderType, item: ItemType)

    /**
     * See: [RecyclerView.Adapter.getItemId], [RecyclerView.Adapter.hasStableIds]
     */
    fun itemId(item: ItemType): Long = NO_ID
  }

  /**
   * Implement on an instance of [BindingDelegate] to receive incremental bindView callbacks
   */
  interface IncrementalBindingDelegate<in ItemType : Any, HolderType : RecyclerView.ViewHolder> {

    /**
     * See: [RecyclerView.Adapter.onBindViewHolder]
     */
    fun bindView(holderType: HolderType, item: ItemType, payloads: List<Any>)
  }

  /**
   * Implement on an instance of [BindingDelegate] to receive onViewRecycled callbacks
   */
  interface OnViewRecycledDelegate<in HolderType : RecyclerView.ViewHolder> {

    /**
     * See: [RecyclerView.Adapter.onViewRecycled]
     */
    fun onRecycle(holder: HolderType)
  }

  /**
   * Implement on an instance of [BindingDelegate] to receive onRecycleFailed callbacks
   */
  interface OnViewRecycleFailedDelegate<in HolderType : RecyclerView.ViewHolder> {

    /**
     * See: [RecyclerView.Adapter.onFailedToRecycleView]
     */
    fun onRecycleFailed(holder: HolderType): Boolean
  }

  /**
   * Implement on an instance of [BindingDelegate] to receive onViewAttached callbacks
   */
  interface OnViewAttachedDelegate<in HolderType : RecyclerView.ViewHolder> {

    /**
     * See: [RecyclerView.Adapter.onViewAttachedToWindow]
     */
    fun onAttach(holder: HolderType)
  }

  /**
   * Implement on an instance of [BindingDelegate] to receive onViewDetached callbacks
   */
  interface OnViewDetachedDelegate<in HolderType : RecyclerView.ViewHolder> {

    /**
     * See: [RecyclerView.Adapter.onViewDetachedFromWindow]
     */
    fun onDetach(holder: HolderType)
  }

  private fun getItem(position: Int) = itemProvider.getItem(position)

  override fun getItemCount() = itemProvider.getItemCount()

  override fun getItemViewType(position: Int): Int =
    getDelegate(getItem(position).javaClass).layoutId

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val delegate = requireNotNull(layoutLookup[viewType]) {
      "No binding delegate found for viewType $viewType." +
        "This should never happen as the delegate provides the viewType as the layout id property"
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

  override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
    return getDelegate(holder.itemViewType).asViewRecycleFailedDelegate()?.onRecycleFailed(holder)
      ?: super.onFailedToRecycleView(holder)
  }

  override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
    getDelegate(holder.itemViewType).asViewAttachedDelegate()?.onAttach(holder)
  }

  override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
    getDelegate(holder.itemViewType).asViewDetachedDelegate()?.onDetach(holder)
  }

  override fun getItemId(position: Int): Long {
    val item = getItem(position)
    val delegate = getDelegate(item.javaClass)
    return delegate.itemId(item)
  }

  inner class CompositeItemCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
      return when (oldItem.javaClass) {
        newItem.javaClass -> {
          getDelegate(newItem.javaClass).itemCallback.areItemsTheSame(oldItem, newItem)
        }
        else -> false
      }
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
      return when (oldItem.javaClass) {
        newItem.javaClass -> {
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
    return layoutLookup[layoutId] as BindingDelegate<Any, RecyclerView.ViewHolder>
  }

  /**
   * Double check locked delegate lookup. Given the recommended background [DiffUtil] processing
   * lookup contention can produce errant
   */
  private fun getDelegate(itemType: Class<*>): BindingDelegate<Any, RecyclerView.ViewHolder> {
    if (typeLookup.containsKey(itemType)) {
      @Suppress("UNCHECKED_CAST")
      return typeLookup[itemType] as BindingDelegate<Any, RecyclerView.ViewHolder>
    } else {
      // Diff util background execution can cause a race condition in delegate creation.
      // This results in collision exceptions being produced in error.
      synchronized(typeLookup) {
        if (typeLookup.containsKey(itemType)) {
          @Suppress("UNCHECKED_CAST")
          return typeLookup[itemType] as BindingDelegate<Any, RecyclerView.ViewHolder>
        }

        val delegateFactory = delegateFactories[itemType]
          ?: throw MissingDelegateException(itemType, delegateFactories.keys)

        val delegate = delegateFactory.get()

        insertDelegate(delegate)

        @Suppress("UNCHECKED_CAST")
        return delegate as BindingDelegate<Any, RecyclerView.ViewHolder>
      }
    }
  }

  private fun insertDelegate(delegate: BindingDelegate<*, *>) {
    typeLookup.put(delegate.dataType, delegate)?.let { existing ->
      throw DataTypeCollisionException(existing, delegate)
    }

    layoutLookup.put(delegate.layoutId, delegate)?.let { existing ->
      throw LayoutIdCollisionException(existing, delegate)
    }
  }

  private inline fun <reified T> BindingDelegate<Any, RecyclerView.ViewHolder>.asType(): T? {
    return this as? T
  }

  private fun BindingDelegate<Any, RecyclerView.ViewHolder>.asIncremental() =
    asType<IncrementalBindingDelegate<Any, RecyclerView.ViewHolder>>()

  private fun BindingDelegate<Any, RecyclerView.ViewHolder>.asViewRecycledDelegate() =
    asType<OnViewRecycledDelegate<RecyclerView.ViewHolder>>()

  private fun BindingDelegate<Any, RecyclerView.ViewHolder>.asViewRecycleFailedDelegate() =
    asType<OnViewRecycleFailedDelegate<RecyclerView.ViewHolder>>()

  private fun BindingDelegate<Any, RecyclerView.ViewHolder>.asViewAttachedDelegate() =
    asType<OnViewAttachedDelegate<RecyclerView.ViewHolder>>()

  private fun BindingDelegate<Any, RecyclerView.ViewHolder>.asViewDetachedDelegate() =
    asType<OnViewDetachedDelegate<RecyclerView.ViewHolder>>()

  abstract class DelegateException(message: String): IllegalStateException(message)

  class MissingDelegateException(itemType: Class<*>, keys: Set<Class<*>>) :
    DelegateException("No delegate factory bound for Class<$itemType>.\n" +
      "Available delegate factory keys: ${keys.joinToString(prefix = "[\n", postfix = "\n]", separator = ",\n    ")}")

  class DataTypeCollisionException(
    existingDelegate: BindingDelegate<*, *>,
    collidingDelegate: BindingDelegate<*, *>
  ) : DelegateException(
    "Data type: '${existingDelegate.dataType}' collides between '$collidingDelegate' and '$existingDelegate'.\n" +
      "This error can be caught at compile time by using dagger to multi bind the delegate providers."
  )

  class LayoutIdCollisionException(
    existingDelegate: BindingDelegate<*, *>,
    collidingDelegate: BindingDelegate<*, *>
  ) : DelegateException(
    "Partial delegate overwrite.\n" +
      "Layout id: '${existingDelegate.layoutId}' collides between '$collidingDelegate' and '$existingDelegate'.\n" +
      "You can use a resource alias to disambiguate multiple data types using the same layout.\n" +
      "`<item name=\"the_alias_name\" type=\"layout\">@layout/the_real_name</item>`"
  )
}
