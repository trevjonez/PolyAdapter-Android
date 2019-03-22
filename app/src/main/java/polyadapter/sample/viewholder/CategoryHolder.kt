package polyadapter.sample.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import polyadapter.sample.R

class CategoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  private val title: TextView = itemView.findViewById(R.id.headerText)

  fun setTitleText(charSequence: CharSequence) {
    title.text = charSequence
  }
}