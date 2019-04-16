package fr.arouillard.epicture.model.Imgur

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Tags(
        var name: String? = null,
        var display_name: String? = null,
        var background_hash: String? = null
) : Parcelable

@Parcelize
data class Image(
        var id: String? = null,
        var title: String? = null,
        var description: String? = null,
        var type: String? = null,
        var animated: Boolean = false,
        var datetime: Long = 0,
        var width: Int = 0,
        var height: Int = 0,
        var size: Int = 0,
        var views: Int = 0,
        var bandwidth: Long = 0,
        var is_album: Boolean = false,
        var vote: String? = null,
        var favorite: Boolean = false,
        var account_url: String? = null,
        var deletehash: String? = null,
        var name: String? = null,
        var link: String? = null,
        var images: ArrayList<Image> = ArrayList(),
        var tags: ArrayList<Tags> = ArrayList()
) : Parcelable