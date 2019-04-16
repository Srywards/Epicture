package fr.arouillard.epicture

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import fr.arouillard.epicture.api.Imgur
import fr.arouillard.epicture.api.Store

class LoginActivity : AppCompatActivity() {

    private var _loginButton: Button? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        _loginButton = findViewById(R.id.btn_login)
        Store.initStore(this)
        if (Store.isAuthorized) {
            onLoginSuccess()
        } else {
            _loginButton!!.setOnClickListener { login() }
        }
    }

    public override fun onResume() {
        super.onResume()

        val uri = intent.data
        if (uri != null && uri.toString().startsWith(Imgur.REDIRECT_URI)) {
            val tmp = Uri.parse("https://epicture?" + uri.fragment.trim())
            Store.set("access_token", tmp.getQueryParameter("access_token"))
            Store.set("refresh_token", tmp.getQueryParameter("refresh_token"))
            Store.set("account_username", tmp.getQueryParameter("account_username"))
            var expireIn = (tmp.getQueryParameter("expires_in")?.toLong() ?: 0) * 1000
            Store.set("expires_in", System.currentTimeMillis() + expireIn)
            if (Store.isAuthorized) {
                onLoginSuccess()
            } else {
                onLoginFailed()
            }
        }
    }

    private fun login() {
        Log.d(TAG, "Login")

        _loginButton!!.isEnabled = false

        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Imgur.AUTHORIZATION_URL)))
    }


    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun onLoginSuccess() {
        _loginButton!!.isEnabled = true
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun onLoginFailed() {
        Toast.makeText(baseContext, "Login failed", Toast.LENGTH_LONG).show()
        _loginButton!!.isEnabled = true
    }

    companion object {
        private val TAG = "LoginActivity"
    }
}