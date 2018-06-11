# 'android.arch.paging:runtime:1.0.0' is an optional dependency, if not included downstream proguard will scream at you.
-dontwarn android.arch.paging.PagedListHacks
-dontwarn com.trevjonez.polyadapter.PolyAdapterKt
-dontwarn com.trevjonez.polyadapter.providers.PolyPagedListProvider
-dontwarn com.trevjonez.polyadapter.providers.PolyPagedListProvider*