package com.deviantart.artviewer.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import com.deviantart.artviewer.R
import com.deviantart.artviewer.data.remote.DeviantArtMediaItem
import com.deviantart.artviewer.ui.activities.MainActivity
import com.deviantart.artviewer.ui.components.Toolbar
import com.deviantart.artviewer.util.ToolbarButtonData
import com.deviantart.artviewer.util.UiState



/**
 * Screen used for the DisplayArtActivity.
 */
@Composable
fun DisplayArtScreen(viewModel: DisplayArtViewModel, folderName: String) {
    val state = viewModel.uiState.collectAsState()


    Column(
        modifier = Modifier.fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Toolbar(folderName)


        val exactState: UiState<List<DeviantArtMediaItem>> = state.value//needed to satisfy compiler type concerns
        when(exactState){
            UiState.Loading -> LoadingDisplay()
            is UiState.Error -> ErrorDisplay(exactState.message)
            is UiState.Success<List<DeviantArtMediaItem>> -> ArtDisplay(exactState.data)
        }
    }
}



/**
 * Version of the toolbar that is specifically set for the DisplayArtScreen.
 */
@Composable
private fun Toolbar(folderName: String){
    val context = LocalContext.current

    Toolbar(
        includeBackButton = true,
        title = folderName,
        otherButtons = listOf(
            ToolbarButtonData(
                icon = R.drawable.ic_home,
                contentDescription = "Home button",
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }
            )
        )
    )
}



@Composable
private fun LoadingDisplay(){
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.loading_art_message),
            fontSize = 26.sp,
            textAlign = TextAlign.Center
        )
    }
}



/**
 * Display for when the viewModel fails to fetch art.
 *
 * Uses a default error message if none is provided.
 */
@Composable
private fun ErrorDisplay(errorMessage: String? = null){
    val actualMessage =
        if (!errorMessage.isNullOrEmpty())
            errorMessage
        else
            stringResource(R.string.default_error_msg)


    Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text(
            text = actualMessage,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}



/**
 * Display each art item in a separate page so you can scroll between
 * them, and they snap into place (like YouTube sorts or TikTok).
 */
@Composable
private fun ArtDisplay(artList: List<DeviantArtMediaItem>) {
    val pagerState = rememberPagerState(pageCount = { artList.size })

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->

        val isCurrentPage = pagerState.currentPage == page
        val artItem = artList[page]


        val videoUrl = artItem.getVideoUrl()
        val imageUrl = artItem.getImageUrl()


        if (!videoUrl.isNullOrEmpty()){
            VideoPlayer(title = artItem.title, url = videoUrl, play = isCurrentPage)
        }
        else if(!imageUrl.isNullOrEmpty()) {
            ImageDisplay(title = artItem.title, url = imageUrl)
        }
    }
}



@Composable
private fun VideoPlayer(title: String, url: String, play: Boolean) {
    val context = LocalContext.current


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


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

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
}



@Composable
private fun ImageDisplay(title: String, url: String){
    val imageLoader = rememberGifImageLoader()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        AsyncImage(
            model = url,
            imageLoader = imageLoader,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}



@Composable
fun rememberGifImageLoader(): ImageLoader {
    val context = LocalContext.current
    return ImageLoader.Builder(context)
        .components {
            add(GifDecoder.Factory())
        }
        .build()
}
