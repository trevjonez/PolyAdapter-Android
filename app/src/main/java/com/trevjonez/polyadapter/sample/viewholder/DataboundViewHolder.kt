package com.trevjonez.polyadapter.sample.viewholder

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView

class DataboundViewHolder<VDB : ViewDataBinding>(val viewBinding: VDB) :
    RecyclerView.ViewHolder(viewBinding.root)