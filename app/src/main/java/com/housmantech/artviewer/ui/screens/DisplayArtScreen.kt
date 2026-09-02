package com.housmantech.artviewer.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
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
import androidx.compose.material3.CircularProgressIndicator
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
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import com.housmantech.artviewer.R
import com.housmantech.artviewer.data.remote.DeviantArtMediaItem
import com.housmantech.artviewer.ui.activities.LoginActivity
import com.housmantech.artviewer.ui.activities.MainActivity
import com.housmantech.artviewer.ui.components.Toolbar
import com.housmantech.artviewer.ui.util.LazyMediaItem
import com.housmantech.artviewer.ui.util.NavDestination
import com.housmantech.artviewer.ui.util.ToolbarButtonData
import com.housmantech.artviewer.ui.util.UiState



/**
 * Screen used for the DisplayArtActivity.
 */
@Composable
fun DisplayArtScreen(viewModel: DisplayArtViewModel, folderName: String) {
    val state = viewModel.uiState.collectAsState()
    val context = LocalContext.current


    LaunchedEffect(Unit){
        viewModel.navigation.collect { destination ->
            when(destination){
                NavDestination.ToLoginActivity -> {
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }

                else -> { }
            }
        }
    }


    Column(
        modifier = Modifier.fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Toolbar(folderName)


        val exactState: UiState<LinkedList> = state.value//needed to satisfy compiler type concerns
        when(exactState){
            UiState.Loading -> LoadingDisplay()
            is UiState.Error -> ErrorDisplay(exactState.message)
            is UiState.Success<List<LazyMediaItem>> -> ArtDisplay(viewModel, exactState.data)
        }
    }
}



/**
 * Version of the toolbar that is specifically set for the DisplayArtScreen.
 */
@Composable
private fun Toolbar(folderName: String){
    val activity = LocalActivity.current



    Toolbar(
        includeBackButton = true,
        title = folderName,
        otherButtons = listOf(
            ToolbarButtonData(
                icon = R.drawable.ic_home,
                contentDescription = stringResource(R.string.home_icon_content_description),
                onClick = {
                    activity?.finish()
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
            text = stringResource(R.string.media_loading_message),
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
            stringResource(R.string.media_default_error_msg)


    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = actualMessage,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}



/**
 * Display each art item in a separate page so you can scroll between
 * them, and they snap into place (like YouTube sorts or TikTok).
 */
@Composable
private fun ArtDisplay(viewModel: DisplayArtViewModel, artList: List<LazyMediaItem>) {
    //TODO: need to handle possibility of an empty folder

    val pagerState = rememberPagerState(pageCount = { artList.size })

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->

        val item = artList[page]
        val isCurrentPage = pagerState.currentPage == page

        LaunchedEffect(page) {
            viewModel.ensureLoaded(page)//TODO do stuff on page scroll
        }


        when (item){
            is LazyMediaItem.Loaded -> {
                val videoUrl = item.media.getVideoUrl()
                val imageUrl = item.media.getImageUrl()

                if (!videoUrl.isNullOrEmpty()) {
                    VideoPlayer(title = item.media.title, url = videoUrl, play = isCurrentPage)
                }
                else if (!imageUrl.isNullOrEmpty()) {
                    ImageDisplay(title = item.media.title, url = imageUrl, showSpinner = isCurrentPage)
                }
            }
            is LazyMediaItem.Pending -> {
                CircularProgressIndicator()
            }
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
private fun ImageDisplay(title: String, url: String, showSpinner: Boolean){
    val imageLoader = rememberGifImageLoader()
    val painter = rememberAsyncImagePainter(
        model = url,
        imageLoader = imageLoader
    )

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

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            if (showSpinner && painter.state is AsyncImagePainter.State.Loading) {
                CircularProgressIndicator()
            }
        }
    }
}



/**
 * Image loader that supports GIF's. Pass this into the AsyncImage
 * composable.
 */
@Composable
fun rememberGifImageLoader(): ImageLoader {
    val context = LocalContext.current
    return ImageLoader.Builder(context)
        .components {
            add(GifDecoder.Factory())
        }
        .build()
}
