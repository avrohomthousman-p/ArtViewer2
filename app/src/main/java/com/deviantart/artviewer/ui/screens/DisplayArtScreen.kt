package com.deviantart.artviewer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.deviantart.artviewer.ui.components.Toolbar


/**
 * Screen used for the DisplayArtActivity.
 */
@Composable
fun DisplayArtScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Toolbar(includeBackButton = true, title = "testrun")

        //TODO: replace with real data
        val videoUrls = listOf(
            "https://wixmp-ed30a86b8c4ca887773594c2.wixmp.com/v/mp4/27c6bab5-855f-4fd0-a29c-4eb2b2d21405/dldn2oe-d80ed949-792a-4b3f-a082-e7bcac86a79e.VideoQualities.res_360p.f8d73f30e7ea4907b607f95d3830e816.mp4",
            "https://wixmp-ed30a86b8c4ca887773594c2.wixmp.com/v/mp4/e9112713-b9dc-4a88-b0c3-1dbe26f51849/dkxi7r4-b5a878ab-7924-48c6-a88d-e0fc587b6e41.VideoQualities.res_360p.2b68ed9dd84d47ca8b0e3211c4567b42.mp4",
            "https://wixmp-ed30a86b8c4ca887773594c2.wixmp.com/v/mp4/103d5233-607d-4aba-bde5-daeb0df3c52b/djtaxhj-796af7d2-94c6-414d-bce8-a171bd4c16d7.VideoQualities.res_360p.d8d5688908b44b72ad06afad0e4471d9.mp4"
        )

        val pagerState = rememberPagerState(pageCount = { videoUrls.size })

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->

            val isCurrentPage = pagerState.currentPage == page
            VideoPlayer(url = videoUrls[page], play = isCurrentPage)
        }
    }
}



@Composable
fun VideoPlayer(url: String, play: Boolean) {
    val context = LocalContext.current

    // Create and remember ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(url)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    LaunchedEffect(play) {
        exoPlayer.playWhenReady = play
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
