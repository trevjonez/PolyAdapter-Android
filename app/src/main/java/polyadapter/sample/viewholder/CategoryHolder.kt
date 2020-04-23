package polyadapter.sample.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import polyadapter.sample.databinding.CategoryItemBinding

class CategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  private val binding = CategoryItemBinding.bind(itemView)

  fun setTitleText(charSequence: CharSequence) {
    binding.headerText.text = charSequence
  }
}