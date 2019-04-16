package fr.arouillard.epicture

import android.os.Bundle
import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.design.widget.NavigationView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.LayoutInflater
import android.net.Uri

import android.text.Editable
import android.text.TextWatcher

import fr.arouillard.epicture.api.Store
import fr.arouillard.epicture.api.AuthService
import kotlinx.android.synthetic.main.activity_main.*

import fr.arouillard.epicture.model.Imgur.Basic as ImgurBasic
import fr.arouillard.epicture.model.Imgur.Image as ImgurImage
import fr.arouillard.epicture.model.Unsplash.Basic as UnsplashBasic
import fr.arouillard.epicture.model.Unsplash.Image as UnsplashImage
import fr.arouillard.epicture.view.ImgurAdapter
import fr.arouillard.epicture.view.UnsplashConverter

import java.net.HttpURLConnection
import java.io.FileNotFoundException

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import okio.Okio
import okhttp3.RequestBody
import okhttp3.MediaType
import android.content.Intent

import com.mancj.materialsearchbar.MaterialSearchBar

import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v4.view.GravityCompat
import fr.arouillard.epicture.view.TagAdapter

import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import fr.arouillard.epicture.model.Imgur.Avatar

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MaterialSearchBar.OnSearchActionListener {

    lateinit var tagAdapter: TagAdapter

    var mode: String = "top"

    var unsplash: Boolean = false

    private val onItemClickListener = object : ImgurAdapter.OnItemClickListener {
        override fun onItemClick(view: View, image: ImgurImage) {
            val intent = DetailActivity.newIntent(this@MainActivity, image)

            val img = view.findViewById<ImageView>(R.id.imgView)
            val nameHolder = view.findViewById<LinearLayout>(R.id.nameHolder)

            val imgP = Pair.create(img as View, "tImage")
            val nameHolderP = Pair.create(nameHolder as View, "tNameHolder")

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@MainActivity,
                    imgP, nameHolderP)
            ActivityCompat.startActivity(this@MainActivity, intent, options.toBundle())
        }

        override fun onFavItemClick(view: View, image: ImgurImage) = favorite(image)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Store.initStore(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        val adapter = ImgurAdapter(this)
        adapter.setOnItemClickListener(onItemClickListener)
        recyclerView.adapter = adapter

        getAccountImages()

        fab.setOnClickListener { selectImage() }

        initSearch()
        initNavBar()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                try {
                    upload(data.data)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun initSearch() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        tagAdapter = TagAdapter(inflater)
        val searchBar = findViewById<MaterialSearchBar>(R.id.searchBar)
        searchBar.setOnSearchActionListener(this)
        searchBar.setSpeechMode(true)
        searchBar.inflateMenu(R.menu.menu_main)
        val clickListener = PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
            when(item?.itemId) {
                R.id.top -> {
                    mode = "top"
                }
                R.id.viral -> {
                    mode = "viral"
                }
                R.id.time -> {
                    mode = "time"
                }
            }
            true
        }
        searchBar.menu.setOnMenuItemClickListener(clickListener)
        searchBar.setCustomSuggestionAdapter(tagAdapter)
        searchBar.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                search(charSequence.toString(), true)
            }

            override fun afterTextChanged(editable: Editable) {

            }

        })
    }

    private fun initNavBar() {
        AuthService.api.avatar("me")
                .enqueue(object : Callback<ImgurBasic<Avatar>> {
                    override fun onResponse(call: Call<ImgurBasic<Avatar>>, response: Response<ImgurBasic<Avatar>>) {
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            Glide.with(nav_view.getHeaderView(0).context)
                                    .load((response.body()?.data as Avatar).avatar)
                                    .apply(RequestOptions().circleCrop())
                                    .into(nav_view.getHeaderView(0).findViewById(R.id.imageView))
                        }
                    }
                    override fun onFailure(call: Call<ImgurBasic<Avatar>>, t: Throwable) {
                    }
                })
        nav_view.menu.findItem(R.id.imgur_only).setVisible(false)
        nav_view.setNavigationItemSelectedListener(this)
        val username = nav_view.getHeaderView(0).findViewById<TextView>(R.id.username)
        username.text = Store.get("account_username")
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), 1)
    }

    private fun favorite(image: ImgurImage) {
        AuthService.api
                .favorite(image.id ?: "")
                .enqueue(object : Callback<ImgurBasic<String>> {
                    override fun onResponse(call: Call<ImgurBasic<String>>, response: Response<ImgurBasic<String>>) {
                        if (response.code() != HttpURLConnection.HTTP_OK) {
                            Toast.makeText(this@MainActivity, "Invalid response", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ImgurBasic<String>>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Got an error", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun upload(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        val img = Okio.buffer(Okio.source(inputStream))
        val image = img.readByteArray()
        val body = RequestBody.create(MediaType.parse("image/jpeg"), image)
        AuthService.api.uploadImage(body)
                .enqueue(object : Callback<ImgurBasic<ImgurImage>> {
                    override fun onResponse(call: Call<ImgurBasic<ImgurImage>>, response: Response<ImgurBasic<ImgurImage>>) {
                        if (response.code() == HttpURLConnection.HTTP_OK) {
                            getAccountImages()
                            Toast.makeText(this@MainActivity, "Uploaded", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Invalid response", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ImgurBasic<ImgurImage>>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Invalid response", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    fun search(query: String, predict: Boolean = false) {
        AuthService.api
                .search(mode, 0, query)
                .enqueue(object : Callback<ImgurBasic<ArrayList<ImgurImage>>> {
                    override fun onResponse(call: Call<ImgurBasic<ArrayList<ImgurImage>>>, response: Response<ImgurBasic<ArrayList<ImgurImage>>>) {
                        val emptyText = findViewById<TextView>(R.id.empty_view)
                        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
                        val data = ArrayList((response.body()?.data ?: ArrayList()))
                        if (predict && response.code() === HttpURLConnection.HTTP_OK) {
                            searchBar.clearSuggestions()
                            for (image in data) {
                                for (tag in image.tags) {
                                    tagAdapter.addSuggestion(tag.display_name)
                                }
                            }
                            if (!searchBar.isSuggestionsVisible && !searchBar.text.isEmpty()) {
                                searchBar.showSuggestionsList()
                            }
                            return
                        }
                        if (response.code() === HttpURLConnection.HTTP_OK) {
                            nav_view.setCheckedItem(R.id.gallery)
                            (recyclerView.adapter as ImgurAdapter).swap(data)
                            if (data.isEmpty()) {
                                recyclerView.visibility = View.GONE
                                emptyText.visibility = View.VISIBLE
                            } else {
                                emptyText.visibility = View.GONE
                            }
                        } else {
                            recyclerView.visibility = View.GONE
                            emptyText.visibility = View.VISIBLE
                            emptyText.text = "Unexpected response"
                        }
                    }

                    override fun onFailure(call: Call<ImgurBasic<ArrayList<ImgurImage>>>, t: Throwable) {
                        val emptyText = findViewById<TextView>(R.id.empty_view)
                        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
                        recyclerView.visibility = View.GONE
                        emptyText.visibility = View.VISIBLE
                        emptyText.text = "Unexpected error"
                        t.printStackTrace()
                    }
                })
    }

    fun getAccountImages() {
        nav_view.setCheckedItem(R.id.gallery)
        if (unsplash) {
            getUnsplashImages()
            return
        }
        AuthService.api
                .images("me")
                .enqueue(object : Callback<ImgurBasic<ArrayList<ImgurImage>>> {
                    override fun onResponse(call: Call<ImgurBasic<ArrayList<ImgurImage>>>, response: Response<ImgurBasic<ArrayList<ImgurImage>>>) {
                        val emptyText = findViewById<TextView>(R.id.empty_view)
                        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
                        val data = response.body()?.data ?: ArrayList()
                        if (response.code() === HttpURLConnection.HTTP_OK) {
                            (recyclerView.adapter as ImgurAdapter).swap(data)
                            if (data.isEmpty()) {
                                recyclerView.visibility = View.GONE
                                emptyText.visibility = View.VISIBLE
                            } else {
                                emptyText.visibility = View.GONE
                            }
                        } else {
                            recyclerView.visibility = View.GONE
                            emptyText.visibility = View.VISIBLE
                            emptyText.text = "Unexpected response"
                        }
                    }

                    override fun onFailure(call: Call<ImgurBasic<ArrayList<ImgurImage>>>, t: Throwable) {
                        val emptyText = findViewById<TextView>(R.id.empty_view)
                        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
                        recyclerView.visibility = View.GONE
                        emptyText.visibility = View.VISIBLE
                        emptyText.text = "Unexpected error"
                        t.printStackTrace()
                    }
                })
    }

    private fun getTrends() {
        nav_view.setCheckedItem(R.id.trends)
        AuthService.api
                .trends(mode)
                .enqueue(object : Callback<ImgurBasic<ArrayList<ImgurImage>>> {
                    override fun onResponse(call: Call<ImgurBasic<ArrayList<ImgurImage>>>, response: Response<ImgurBasic<ArrayList<ImgurImage>>>) {
                        val emptyText = findViewById<TextView>(R.id.empty_view)
                        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
                        val data = response.body()?.data ?: ArrayList()
                        if (response.code() === HttpURLConnection.HTTP_OK) {
                            (recyclerView.adapter as ImgurAdapter).swap(data)
                            if (data.isEmpty()) {
                                recyclerView.visibility = View.GONE
                                emptyText.visibility = View.VISIBLE
                            } else {
                                emptyText.visibility = View.GONE
                            }
                        } else {
                            recyclerView.visibility = View.GONE
                            emptyText.visibility = View.VISIBLE
                            emptyText.text = "Unexpected response"
                        }
                    }

                    override fun onFailure(call: Call<ImgurBasic<ArrayList<ImgurImage>>>, t: Throwable) {
                        val emptyText = findViewById<TextView>(R.id.empty_view)
                        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
                        recyclerView.visibility = View.GONE
                        emptyText.visibility = View.VISIBLE
                        emptyText.text = "Unexpected error"
                        t.printStackTrace()
                    }
                })

    }

    private fun getFavorites() {
        nav_view.setCheckedItem(R.id.favorite)
        AuthService.api
                .favorites("me", 0)
                .enqueue(object : Callback<ImgurBasic<ArrayList<ImgurImage>>> {
                    override fun onResponse(call: Call<ImgurBasic<ArrayList<ImgurImage>>>, response: Response<ImgurBasic<ArrayList<ImgurImage>>>) {
                        val emptyText = findViewById<TextView>(R.id.empty_view)
                        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
                        val data = response.body()?.data ?: ArrayList()
                        if (response.code() === HttpURLConnection.HTTP_OK) {
                            (recyclerView.adapter as ImgurAdapter).swap(data)
                            if (data.isEmpty()) {
                                recyclerView.visibility = View.GONE
                                emptyText.visibility = View.VISIBLE
                            } else {
                                emptyText.visibility = View.GONE
                            }
                        } else {
                            recyclerView.visibility = View.GONE
                            emptyText.visibility = View.VISIBLE
                            emptyText.text = "Unexpected response"
                        }
                    }

                    override fun onFailure(call: Call<ImgurBasic<ArrayList<ImgurImage>>>, t: Throwable) {
                        val emptyText = findViewById<TextView>(R.id.empty_view)
                        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
                        recyclerView.visibility = View.GONE
                        emptyText.visibility = View.VISIBLE
                        emptyText.text = "Unexpected error"
                        t.printStackTrace()
                    }
                })
    }

    private fun getUnsplashImages() {
        nav_view.setCheckedItem(R.id.gallery)
        AuthService.unsplash
                .photos()
                .enqueue(object : Callback<ArrayList<UnsplashImage>> {
                    override fun onResponse(call: Call<ArrayList<UnsplashImage>>, response: Response<ArrayList<UnsplashImage>>) {
                        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
                        val data = response.body() ?: ArrayList()
                        if (response.code() === HttpURLConnection.HTTP_OK) {
                            (recyclerView.adapter as ImgurAdapter).swap(UnsplashConverter.convert(data))
                        }
                    }

                    override fun onFailure(call: Call<ArrayList<UnsplashImage>>, t: Throwable) {
                    }
                })
    }

    private fun searchUnsplashImages(query: String) {
        AuthService.unsplash
                .search(1, query)
                .enqueue(object : Callback<UnsplashBasic<ArrayList<UnsplashImage>>> {
                    override fun onResponse(call: Call<UnsplashBasic<ArrayList<UnsplashImage>>>, response: Response<UnsplashBasic<ArrayList<UnsplashImage>>>) {
                        val emptyText = findViewById<TextView>(R.id.empty_view)
                        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
                        val data = response.body()  ?.results ?: ArrayList()
                        if (response.code() === HttpURLConnection.HTTP_OK) {
                            (recyclerView.adapter as ImgurAdapter).swap(UnsplashConverter.convert(data))
                        }
                    }

                    override  fun onFailure(call: Call<UnsplashBasic<ArrayList<UnsplashImage>>>, t: Throwable) {
                    }
                })

    }

    private fun onDisconnect(): Boolean {
        Store.set("expires_in", System.currentTimeMillis())
        startActivity(Intent(this, LoginActivity::class.java))
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.nav_camera -> {
                selectImage()
                drawer_layout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.gallery -> {
                getAccountImages()
                drawer_layout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.favorite -> {
                getFavorites()
                drawer_layout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.trends -> {
                getTrends()
                drawer_layout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.unsplash_only -> {
                unsplash = true
                drawer_layout.closeDrawer(GravityCompat.START)
                nav_view.menu.findItem(R.id.imgur_only).setVisible(true)
                nav_view.menu.findItem(R.id.unsplash_only).setVisible(false)
                nav_view.menu.findItem(R.id.nav_camera).setVisible(false)
                nav_view.menu.findItem(R.id.favorite).setVisible(false)
                nav_view.menu.findItem(R.id.trends).setVisible(false)
                getAccountImages()
                return false
            }
            R.id.imgur_only -> {
                unsplash = false
                drawer_layout.closeDrawer(GravityCompat.START)
                nav_view.menu.findItem(R.id.imgur_only).setVisible(false)
                nav_view.menu.findItem(R.id.unsplash_only).setVisible(true)
                nav_view.menu.findItem(R.id.nav_camera).setVisible(true)
                nav_view.menu.findItem(R.id.favorite).setVisible(true)
                nav_view.menu.findItem(R.id.trends).setVisible(true)
                getAccountImages()
                return false
            }
            R.id.disconnect -> onDisconnect()
            else -> false
        }
    }


    override fun onButtonClicked(buttonCode: Int) {
        when(buttonCode) {
            MaterialSearchBar.BUTTON_NAVIGATION -> drawer_layout.openDrawer(GravityCompat.START)
            MaterialSearchBar.BUTTON_SPEECH -> return
            MaterialSearchBar.BUTTON_BACK -> searchBar.disableSearch()
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSearchStateChanged(enabled: Boolean) {}

    override fun onSearchConfirmed(text: CharSequence) {
        if (text.isEmpty()) {
            getAccountImages()
        } else {
            if (unsplash) {
                searchUnsplashImages(text.toString())
            } else {
                search(text.toString())
            }
        }
        searchBar.disableSearch()
        searchBar.hideSuggestionsList()
    }
}
