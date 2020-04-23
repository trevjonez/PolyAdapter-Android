package polyadapter.sample.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import polyadapter.sample.databinding.MovieItemBinding

class MovieHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  val viewBinding = MovieItemBinding.bind(itemView)
}
