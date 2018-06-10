package com.trevjonez.polyadapter.sample.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.trevjonez.polyadapter.R

class CategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  private val title: TextView = itemView.findViewById(R.id.headerText)

  fun setTitleText(charSequence: CharSequence) {
    title.text = charSequence
  }
}