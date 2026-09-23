package com.housmantech.artviewer.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
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
import com.housmantech.artviewer.ui.activities.SettingsActivity
import com.housmantech.artviewer.ui.components.ParagraphWithLinks
import com.housmantech.artviewer.ui.components.Toolbar
import com.housmantech.artviewer.ui.themes.AppColors
import com.housmantech.artviewer.ui.util.NavDestination
import com.housmantech.artviewer.ui.util.OrientationLayout
import com.housmantech.artviewer.ui.util.ToolbarButtonData
import com.housmantech.artviewer.ui.util.UiState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds


/**
 * Screen used for the DisplayArtActivity.
 */
@Composable
fun DisplayArtScreen(
    viewModel: DisplayArtViewModel,
    isLandscape: Boolean,
    folderName: String
) {
    val activity = LocalActivity.current as? ComponentActivity
    val context = LocalContext.current
    val state = viewModel.uiState.collectAsState()
    val isFullscreenMode = isLandscape && state.value is UiState.Success
    val matureContentAllowed by viewModel.matureContentAllowed.collectAsState()


    val statusBarColor =
        if (isFullscreenMode)
            AppColors.NavBarColor
        else
            AppColors.StatusBarColor


    //Ensure the edge to edge is handled correctly in the case of landscape mode and Success state
    SideEffect {
        if (isFullscreenMode) {
            activity?.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    scrim = statusBarColor.toArgb(),
                    darkScrim = statusBarColor.toArgb()
                ),
                navigationBarStyle = SystemBarStyle.light(
                    scrim = AppColors.NavBarColor.toArgb(),
                    darkScrim = AppColors.NavBarColor.toArgb()
                )
            )
        }
    }


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
        if (!isFullscreenMode) {
            Toolbar(folderName)
        }



        val exactState: UiState<List<DeviantArtMediaItem>> = state.value//needed to satisfy compiler type concerns
        when(exactState){
            UiState.Loading -> LoadingDisplay(matureContentAllowed)
            is UiState.Error -> ErrorDisplay(exactState.message)
            is UiState.Success<List<DeviantArtMediaItem>> -> ArtDisplay(
                artList = exactState.data,
                onScroll = { page, isForward ->
                    viewModel.onScroll(page, isForward)
                }
            )
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
private fun LoadingDisplay(matureContentAllowed: Boolean){
    var isTakingLong by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(5500.milliseconds)
        isTakingLong = true
    }


    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.media_loading_message),
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )


        if (isTakingLong){
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = stringResource(R.string.media_slow_loading_title),
                fontSize = 20.sp
            )

            if (!matureContentAllowed) {
                val activity = LocalActivity.current
                val context = LocalContext.current

                val navigateToSettings: () -> Unit = {
                    activity?.let {
                        val intent = Intent(context, SettingsActivity::class.java)
                        it.startActivity(intent)
                        it.finish()
                    }
                }


                val fullText = stringResource(R.string.media_slow_loading_mature_warning)
                val linkText = stringResource(R.string.media_slow_loading_hyperlink_text)

                Spacer(modifier = Modifier.height(16.dp))
                ParagraphWithLinks(
                    fullText = fullText,
                    links = mapOf(
                        linkText to navigateToSettings
                    )
                )
            }
        }
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
        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
private fun ArtDisplay(
    artList: List<DeviantArtMediaItem>,
    onScroll: (Int, Boolean) -> Unit
) {
    if (artList.isEmpty()) {
        ErrorDisplay(errorMessage = stringResource(R.string.media_folder_empty_message))
        return
    }


    val pagerState = rememberPagerState(pageCount = { artList.size })


    LaunchedEffect(pagerState) {
        var lastPage = pagerState.currentPage

        snapshotFlow { pagerState.currentPage }
            .collect { newPage ->
                val isForward = newPage > lastPage
                onScroll(newPage, isForward)
                lastPage = newPage
            }
    }


    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->


        val isCurrentPage = pagerState.currentPage == page
        val artItem = artList[page]


        val videoUrl = artItem.getVideoUrl()
        val imageUrl = artItem.getImageUrl()


        if (!videoUrl.isNullOrEmpty()){
            VideoPlayerContainer(title = artItem.title, url = videoUrl, play = isCurrentPage)
        }
        else if(!imageUrl.isNullOrEmpty()) {
            ImageDisplayContainer(title = artItem.title, url = imageUrl, showSpinner = isCurrentPage)
        }
    }
}



@Composable
private fun VideoPlayerContainer(title: String, url: String, play: Boolean) {
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



    OrientationLayout(
        portrait = {
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

                VideoPlayer(exoPlayer)
            }
        },
        landscape = {
            VideoPlayer(exoPlayer)
        }
    )
}



/**
 * Displays the actual video with no containers or anything
 */
@Composable
private fun VideoPlayer(exoPlayer: ExoPlayer) {
    val context = LocalContext.current

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



@Composable
private fun ImageDisplayContainer(title: String, url: String, showSpinner: Boolean){
    val imageLoader = rememberGifImageLoader()
    val painter = rememberAsyncImagePainter(
        model = url,
        imageLoader = imageLoader
    )


    OrientationLayout(
        portrait = {
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

                ImageDisplay(painter, showSpinner)
            }
        },
        landscape = {
            ImageDisplay(painter, showSpinner)
        }
    )
}



@Composable
private fun ImageDisplay(painter: AsyncImagePainter, showSpinner: Boolean) {
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
