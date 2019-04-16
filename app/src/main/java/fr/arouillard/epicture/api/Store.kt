package fr.arouillard.epicture.api

import android.content.Context
import android.content.SharedPreferences

object Store {

    private var store: SharedPreferences? = null

    val isAuthorized: Boolean
        get() = get("access_token") != null && getLong("expires_in") > System.currentTimeMillis()

    fun initStore(context: Context) {
        store = context.getSharedPreferences("oauth", Context.MODE_PRIVATE)
    }

    private fun editStore(): SharedPreferences.Editor {
        return store!!.edit()
    }

    operator fun get(key: String): String? {
        return store?.getString(key, null)
    }

    fun getLong(key: String): Long {
        return store!!.getLong(key, -1)
    }

    operator fun set(key: String, value: String) {
        editStore().putString(key, value).commit()
    }

    operator fun set(key: String, value: Long?) {
        editStore().putLong(key, value!!).commit()
    }


}