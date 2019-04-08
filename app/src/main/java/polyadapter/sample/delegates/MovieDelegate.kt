package polyadapter.sample.delegates

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import polyadapter.PolyAdapter
import polyadapter.sample.GlideApp
import polyadapter.sample.R
import polyadapter.sample.data.Movie
import polyadapter.sample.databinding.MovieItemBinding
import polyadapter.sample.databinding.MovieItemBinding.bind
import polyadapter.sample.viewholder.DataboundViewHolder
import javax.inject.Inject

class MovieDelegate @Inject constructor() : PolyAdapter.BindingDelegate<Movie, DataboundViewHolder<MovieItemBinding>> {
  override val layoutId = R.layout.movie_item
  override val dataType = Movie::class.java
  override val itemCallback = object : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
      return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
      return oldItem == newItem
    }
  }

  override fun createViewHolder(itemView: View): DataboundViewHolder<MovieItemBinding> =
      DataboundViewHolder(bind(itemView)!!)

  override fun bindView(holder: DataboundViewHolder<MovieItemBinding>, item: Movie) {
    holder.viewBinding.apply {
      movieTitle.text = item.title
      webLink.setOnClickListener {
        it.context.startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(item.url) })
      }

      GlideApp.with(movieImage)
          .load(item.imgUrl)
          .placeholder(R.drawable.ic_image_black_24dp)
          .error(R.drawable.ic_broken_image_black_24dp)
          .centerInside()
          .into(movieImage)
    }
  }
}