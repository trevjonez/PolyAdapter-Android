package polyadapter.sample.delegates

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import polyadapter.PolyAdapter
import polyadapter.sample.R
import polyadapter.sample.data.DividerLine
import polyadapter.sample.viewholder.DividerHolder
import javax.inject.Inject

class DividerDelegate @Inject constructor() : PolyAdapter.BindingDelegate<DividerLine, DividerHolder> {
  override val layoutId = R.layout.divider_line
  override val dataType = DividerLine::class.java
  override val itemCallback = object : DiffUtil.ItemCallback<DividerLine>() {
    override fun areItemsTheSame(oldItem: DividerLine, newItem: DividerLine): Boolean {
      return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: DividerLine, newItem: DividerLine): Boolean {
      return oldItem === newItem
    }
  }
  override fun createViewHolder(itemView: View) = DividerHolder(itemView)
  override fun bindView(holder: DividerHolder, item: DividerLine) {}
}

