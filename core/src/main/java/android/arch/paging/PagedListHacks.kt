@file:JvmName("PagedListHacks")

package android.arch.paging

val <T> PagedList<T>.contiguous: Boolean
  get() = isContiguous

val <T> PagedList<T>.computeLeadingNulls: Int
  get() = mStorage.computeLeadingNulls()

val <T> PagedList<T>.leadingNulls: Int
  get() = mStorage.leadingNullCount

val <T> PagedList<T>.computeTrailingNulls: Int
  get() = mStorage.computeTrailingNulls()