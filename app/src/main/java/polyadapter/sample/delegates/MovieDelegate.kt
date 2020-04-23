package polyadapter.sample.delegates

import android.content.Intent
import android.net.Uri
import android.view.View
import polyadapter.PolyAdapter
import polyadapter.equalityItemCallback
import polyadapter.sample.GlideApp
import polyadapter.sample.R
import polyadapter.sample.data.Movie
import polyadapter.sample.viewholder.MovieHolder
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

      GlideApp.with(movieImage)
        .load(item.imgUrl)
        .placeholder(R.drawable.ic_image_black_24dp)
        .error(R.drawable.ic_broken_image_black_24dp)
        .centerInside()
        .into(movieImage)
    }
  }
}
