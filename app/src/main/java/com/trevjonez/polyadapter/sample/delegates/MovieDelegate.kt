package com.trevjonez.polyadapter.sample.delegates

import android.content.Intent
import android.databinding.DataBindingUtil.getBinding
import android.net.Uri
import android.support.v7.util.DiffUtil
import android.view.View
import com.trevjonez.polyadapter.PolyAdapter
import com.trevjonez.polyadapter.R
import com.trevjonez.polyadapter.databinding.MovieItemBinding
import com.trevjonez.polyadapter.sample.data.Movie
import com.trevjonez.polyadapter.sample.viewholder.DataboundViewHolder

class MovieDelegate : PolyAdapter.BindingDelegate<Movie, DataboundViewHolder<MovieItemBinding>> {
  override val layoutId = R.layout.movie_item
  override val dataType = Movie::class.java
  override val itemCallback = object : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie?, newItem: Movie?): Boolean {
      return oldItem?.title == newItem?.title
    }

    override fun areContentsTheSame(oldItem: Movie?, newItem: Movie?): Boolean {
      return oldItem == newItem
    }
  }

  override fun createViewHolder(itemView: View): DataboundViewHolder<MovieItemBinding> =
    DataboundViewHolder(getBinding(itemView)!!)

  override fun bindView(holder: DataboundViewHolder<MovieItemBinding>, item: Movie) {
    holder.viewBinding.apply {
      movieTitle.text = item.title
      webLink.setOnClickListener {
        it.context.startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(item.url) })
      }
      TODO("Glide img load here")
    }
  }
}