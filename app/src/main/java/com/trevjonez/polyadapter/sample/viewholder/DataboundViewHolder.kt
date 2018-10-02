package com.trevjonez.polyadapter.sample.viewholder

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class DataboundViewHolder<VDB : ViewDataBinding>(val viewBinding: VDB) :
    RecyclerView.ViewHolder(viewBinding.root)