package polyadapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

@SuppressLint("DiffUtilEquals")
fun <T : Any> equalityItemCallback(): DiffUtil.ItemCallback<T> =
  object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem === newItem
    override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
  }

@SuppressLint("DiffUtilEquals")
inline fun <T : Any> equalityItemCallback(crossinline identifier: T.() -> Any): DiffUtil.ItemCallback<T> =
  object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T) =
      oldItem.identifier() == newItem.identifier()

    override fun areContentsTheSame(oldItem: T, newItem: T) =
      oldItem == newItem
  }
