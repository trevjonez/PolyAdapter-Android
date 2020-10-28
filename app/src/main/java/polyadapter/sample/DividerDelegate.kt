package polyadapter.sample

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import polyadapter.PolyAdapter
import polyadapter.equalityItemCallback
import javax.inject.Inject

class DividerDelegate @Inject constructor() : PolyAdapter.BindingDelegate<DividerLine, DividerHolder> {
  override val layoutId = R.layout.divider_line
  override val dataType = DividerLine::class.java
  override val itemCallback = equalityItemCallback<DividerLine> { hashCode() }

  override fun createViewHolder(itemView: View) = DividerHolder(itemView)
  override fun bindView(holder: DividerHolder, item: DividerLine) {}
}

class DividerHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class DividerLine