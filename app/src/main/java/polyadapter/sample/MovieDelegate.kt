package polyadapter.sample

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import polyadapter.PolyAdapter
import polyadapter.equalityItemCallback
import polyadapter.sample.databinding.MovieItemBinding
import javax.inject.Inject

class MovieDelegate @Inject constructor() : PolyAdapter.BindingDelegate<Movie, MovieHolder> {
  override val layoutId = R.layout.movie_item
  override val dataType = Movie::class.java
  override val itemCallback = equalityItemCallback<Movie> { title }

  override fun createViewHolder(itemView: View) = MovieHolder(itemView)

  override fun bindView(holder: MovieHolder, item: Movie) {
    holder.viewBinding.apply {
      movieTitle.text = item.title
      webLink.setOnClickListener {
        it.context.startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(item.url) })
      }

      movieImage.apply {
        contentDescription = context.getString(
          R.string.movie_poster_content_description,
          item.title
        )

        load(item.imgUrl) {
          placeholder(R.drawable.ic_image_black_24dp)
          error(R.drawable.ic_broken_image_black_24dp)
        }
      }
    }
  }
}

class MovieHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  val viewBinding = MovieItemBinding.bind(itemView)
}

data class Movie(
  val title: CharSequence,
  val url: String,
  val imgUrl: String
)