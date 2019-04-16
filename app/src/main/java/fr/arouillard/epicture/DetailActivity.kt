package fr.arouillard.epicture

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.Nullable
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette

import fr.arouillard.epicture.api.Store

import fr.arouillard.epicture.model.Imgur.Image

import kotlinx.android.synthetic.main.activity_detail.*

import java.util.Date
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class DetailActivity : AppCompatActivity() {

    lateinit var image: Image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        Store.initStore(this)

        var contextColor = ContextCompat.getColor(this, android.R.color.black)

        image = intent.extras.getParcelable("image") as Image
        textView.text = image.title ?: image.name ?: "Unnamed image"
        description.text = image.description
        if (image.datetime != 0L) {
            date.text = "Added on " + Date(image.datetime*1000)
        }
        author_views.text = "By " + image.account_url + " - " + image.views.toString() + " views"
        var link = image.link
        if (image.is_album) {
            if (image.images.size == 0) {
                return
            }
            link = image.images[0].link
        }
        Glide.with(this)
                .load(link)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(@Nullable e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        //on load failed
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        val bitmap: Bitmap? = if (resource is GifDrawable) resource.firstFrame else (resource as BitmapDrawable).bitmap
                        if (bitmap != null) {
                            val bgColor = Palette.from(bitmap).generate().getMutedColor(contextColor)
                            nameHolder.setBackgroundColor(bgColor)
                        }
                        return false
                    }
                })
                .into(imgView)
    }

    companion object {
        fun newIntent(context: Context, image: Image): Intent {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("image", image)
            return intent
        }
    }
}
