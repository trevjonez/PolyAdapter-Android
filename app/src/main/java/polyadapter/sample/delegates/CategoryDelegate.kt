package polyadapter.sample.delegates

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import polyadapter.PolyAdapter
import polyadapter.sample.R
import polyadapter.sample.data.CategoryTitle
import polyadapter.sample.viewholder.CategoryHolder
import javax.inject.Inject

class CategoryDelegate @Inject constructor() : PolyAdapter.BindingDelegate<CategoryTitle, CategoryHolder> {
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

