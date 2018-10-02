package com.trevjonez.polyadapter.sample.delegates

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.trevjonez.polyadapter.PolyAdapter
import com.trevjonez.polyadapter.R
import com.trevjonez.polyadapter.sample.data.CategoryTitle
import com.trevjonez.polyadapter.sample.viewholder.CategoryHolder

class CategoryDelegate : PolyAdapter.BindingDelegate<CategoryTitle, CategoryHolder> {
  override val layoutId = R.layout.category_item
  override val dataType = CategoryTitle::class.java
  override val itemCallback = object : DiffUtil.ItemCallback<CategoryTitle>() {
    override fun areItemsTheSame(oldItem: CategoryTitle, newItem: CategoryTitle) =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: CategoryTitle, newItem: CategoryTitle) =
        oldItem == newItem
  }

  override fun createViewHolder(itemView: View): CategoryHolder {
    return CategoryHolder(itemView)
  }

  override fun bindView(holder: CategoryHolder, item: CategoryTitle) {
    holder.setTitleText(item.text)
  }
}

