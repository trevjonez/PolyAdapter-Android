package com.trevjonez.polyadapter.sample.delegates

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import com.trevjonez.polyadapter.PolyAdapter
import com.trevjonez.polyadapter.R
import com.trevjonez.polyadapter.sample.data.DividerLine
import com.trevjonez.polyadapter.sample.viewholder.DividerHolder

class DividerDelegate : PolyAdapter.BindingDelegate<DividerLine, DividerHolder> {
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

