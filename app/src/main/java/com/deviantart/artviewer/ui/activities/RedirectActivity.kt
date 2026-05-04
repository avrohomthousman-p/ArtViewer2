package com.deviantart.artviewer.ui.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.compose.setContent
import com.deviantart.artviewer.ui.screens.RedirectScreen


/**
 * The DeviantArt OAuth2.1 login page redirects users here, along with an
 * authorization code that can be used to get an access token.
 */
class RedirectActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        //TODO: extract code value from URL
        //artviewer://oauth2redirect?code=...&iss=https%3A%2F%2Fwww.deviantart.com

        setContent { RedirectScreen() }
    }
}