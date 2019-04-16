package fr.arouillard.epicture.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.graphics.Palette
import android.support.v4.content.ContextCompat
import android.support.annotation.Nullable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import fr.arouillard.epicture.R
import fr.arouillard.epicture.model.Imgur.Image
import fr.arouillard.epicture.model.Unsplash.Image as UnsplashImage

import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.resource.gif.GifDrawable

import android.widget.TextView
import kotlinx.android.synthetic.main.row_layout.view.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.widget.ImageButton

import fr.arouillard.epicture.api.Store

open class ImgurAdapter(context: Context) : RecyclerView.Adapter<ImgurAdapter.ImageHolder>() {

    private var images: ArrayList<Image> = ArrayList()

    private var inflater: LayoutInflater = LayoutInflater.from(context)

    private var contextColor = ContextCompat.getColor(context, android.R.color.black)

    lateinit var itemClickListener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        return ImageHolder(inflater.inflate(R.layout.row_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ImageHolder, idx: Int) {
        onFavorite(holder.itemView, images[idx], false)
        if (images[idx].id == "-1" || images[idx].account_url == Store.get("account_username")) {
            holder.favBtn.visibility = View.GONE
        } else {
            holder.favBtn.visibility = View.VISIBLE
        }
        var link = images[idx].link
        if (images[idx].is_album) {
            if (images[idx].images.size == 0) {
                return
            }
            link = images[idx].images[0].link
        }
        Glide.with(holder.imageView.context)
                .load(link)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(@Nullable e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        //on load failed
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        if (images[idx].title != null && !images[idx].title!!.isEmpty()) {
                            val bitmap : Bitmap? = if (resource is GifDrawable) resource.firstFrame else (resource as BitmapDrawable).bitmap
                            if (bitmap != null) {
                                val bgColor = Palette.from(bitmap).generate().getMutedColor(contextColor)
                                holder.itemView.nameHolder.setBackgroundColor(bgColor)
                                holder.textView.text = images[idx].title
                            }
                        } else {
                            holder.textView.text = ""
                            holder.itemView.nameHolder.setBackgroundColor(0)
                        }
                        return false
                    }
                })
                .into(holder.imageView)
    }

    override fun getItemCount() = images.size

    fun swap(list: ArrayList<Image>?) {
        if (list != null) {
            images = list
            notifyDataSetChanged()
        }
    }

    inner class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        internal var imageView: ImageView = itemView.imgView
        internal var textView: TextView = itemView.text_view
        internal var favBtn: ImageButton = itemView.fav

        init {
            itemView.placeHolder.setOnClickListener(this)
            itemView.fav.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (view == itemView.placeHolder) {
                itemClickListener.onItemClick(itemView, images[adapterPosition])
            } else if (view == itemView.fav) {
                onFavorite(view, images[adapterPosition])
                itemClickListener.onFavItemClick(itemView, images[adapterPosition])
            }
        }
    }

    fun onFavorite(view: View, image: Image, commit: Boolean = true) {
        if (commit) image.favorite = !image.favorite
        if (!image.favorite) {
            (view.fav as ImageButton).setImageResource(R.drawable.ic_favorite_border_black_24dp)
            (view.fav as ImageButton).setColorFilter(ContextCompat.getColor(view.context, R.color.white))
        } else {
            (view.fav as ImageButton).setImageResource(R.drawable.ic_favorite_black_24dp)
            (view.fav as ImageButton).setColorFilter(ContextCompat.getColor(view.context, R.color.colorAccent))
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, image: Image)
        fun onFavItemClick(view: View, image: Image)
    }

    fun setOnItemClickListener(itemClickListener: OnItemClickListener) {
        this.itemClickListener = itemClickListener
    }
}