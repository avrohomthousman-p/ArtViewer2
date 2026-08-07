package com.housmantech.artviewer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


/**
 * Required by hilt, so it can do its setup
 */
@HiltAndroidApp
class ArtViewerApp : Application() {

}