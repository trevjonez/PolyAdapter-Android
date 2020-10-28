package polyadapter.sample

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import polyadapter.PolyAdapter
import polyadapter.equalityItemCallback
import polyadapter.sample.databinding.CategoryItemBinding
import javax.inject.Inject

class CategoryDelegate @Inject constructor() : PolyAdapter.BindingDelegate<CategoryTitle, CategoryHolder> {
  override val layoutId = R.layout.category_item
  override val dataType = CategoryTitle::class.java
  override val itemCallback = equalityItemCallback<CategoryTitle>()

  override fun createViewHolder(itemView: View): CategoryHolder {
    return CategoryHolder(itemView)
  }

  override fun bindView(holder: CategoryHolder, item: CategoryTitle) {
    holder.setTitleText(item.text)
  }
}

class CategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  private val binding = CategoryItemBinding.bind(itemView)

  fun setTitleText(charSequence: CharSequence) {
    binding.headerText.text = charSequence
  }
}

data class CategoryTitle(
  val text: CharSequence
)