package fr.arouillard.epicture.model.Unsplash

class Urls {
    var raw: String? = null
    var full: String? = null
    var regular: String? = null
    var small: String? = null
    var thumb: String? = null
}

class Links {
    var self: String? = null
    var html: String? = null
    var download: String? = null
}

class User {
    var name: String? = null
    var username: String? = null
}

class Image {
    var id: String? = null
    var description: String? = null
    var created_at: String? = null
    var color: String? = null
    var type: String? = null
    var width: Int = 0
    var height: Int = 0
    var likes: Int = 0
    var liked_by_user: Boolean = false
    var urls: Urls? = null
    var links: Links? = null
    var user: User? = null
}