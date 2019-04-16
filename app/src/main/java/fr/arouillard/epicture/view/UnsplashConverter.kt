package fr.arouillard.epicture.view

import fr.arouillard.epicture.model.Imgur.Image as ImgurImage
import fr.arouillard.epicture.model.Unsplash.Image as UnsplashImage

class UnsplashConverter {

    companion object {
        fun convert(list: ArrayList<UnsplashImage>?): ArrayList<ImgurImage> {
            var images: ArrayList<ImgurImage> = ArrayList()
            if (list != null) {
                for (e in list) {
                    val image = ImgurImage()
                    image.id = "-1"
                    image.title = e.description
                    image.link = e.urls?.regular
                    image.views = e.likes
                    image.account_url = e.user?.name ?: e.user?.username ?: "Unknown author"
                    images.add(image)
                }
            }
            return images
        }
    }

}