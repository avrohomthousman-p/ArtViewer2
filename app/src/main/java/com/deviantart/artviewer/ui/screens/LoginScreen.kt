package com.deviantart.artviewer.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.deviantart.artviewer.R
import com.deviantart.artviewer.ui.activities.MainActivity
import com.deviantart.artviewer.ui.components.StandardButton
import com.deviantart.artviewer.ui.components.TOOLBAR_HEIGHT
import com.deviantart.artviewer.ui.components.Toolbar
import com.deviantart.artviewer.ui.themes.AppColors
import com.deviantart.artviewer.util.NavDestination


/**
 * Screen for the login activity.
 */
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val context = LocalContext.current

    // Navigation
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { destination ->
            when (destination) {
                is NavDestination.ToWebLogin -> {
                    val intent = CustomTabsIntent.Builder().build()
                    intent.launchUrl(context, destination.url)
                }

                NavDestination.ToMainActivity -> {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }

                else -> {}
            }
        }
    }


    LoginScreenContent(
        loginState = viewModel.loginState,
        performLogin = { viewModel.triggerLoginStart() }
    )
}


/**
 * Preview friendly screen content
 */
@Composable
private fun LoginScreenContent(
    loginState: LoginState,
    performLogin: () -> Unit
){
    val bannerText = when (loginState) {
        LoginState.LoggedOut -> stringResource(R.string.banner_text_reauthenticate)
        LoginState.LoginInProgress -> stringResource(R.string.banner_text_refreshing_access)
        LoginState.LoginSuccess -> stringResource(R.string.welcome_message)
        LoginState.LoginFailure -> stringResource(R.string.banner_text_refresh_failed)
    }
    val statusText = when (loginState) {
        LoginState.LoggedOut -> null
        LoginState.LoginInProgress -> stringResource(R.string.login_pending_text)
        LoginState.LoginSuccess -> stringResource(R.string.successful_login_text)
        LoginState.LoginFailure -> stringResource(R.string.failed_login_text)
    }
    val statusTextColor = when (loginState) {
        LoginState.LoginSuccess -> AppColors.GreenSuccessColor
        LoginState.LoginFailure -> AppColors.RedErrorColor
        else -> AppColors.Black
    }


    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.White)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        val (toolbar, title, statusContent) = createRefs()

        Toolbar(
            Modifier.constrainAs(toolbar) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        Title(
            text = bannerText,
            modifier = Modifier.constrainAs(title) {
                top.linkTo(toolbar.bottom, margin = 22.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        CenterContent(
            loginState = loginState,
            performLogin = performLogin,
            statusText = statusText,
            statusTextColor = statusTextColor,
            modifier = Modifier.constrainAs(statusContent) {
                top.linkTo(toolbar.bottom)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )
    }
}


@Composable
private fun Toolbar(modifier: Modifier){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(TOOLBAR_HEIGHT.dp)
            .then(modifier)
    ){
        Toolbar(
            includeBackButton = false,
            title = stringResource(R.string.app_name)
        )
    }
}


@Composable
private fun Title(text: String, modifier: Modifier){
    Text(
        text = text,
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .then(modifier),
        fontSize = 22.sp,
        textAlign = TextAlign.Center
    )
}


@Composable
private fun CenterContent(
    loginState: LoginState,
    performLogin: () -> Unit,
    statusText: String?,
    statusTextColor: Color,
    modifier: Modifier
) {
    if (loginState == LoginState.LoggedOut) {
        StandardButton(
            modifier = modifier,
            text = stringResource(R.string.login_btn_text),
            onClick = { performLogin() }
        )
    }
    else if(!statusText.isNullOrBlank()) {
        Text(
            text = statusText,
            modifier = modifier,
            color = statusTextColor,
            fontSize = 22.sp
        )
    }
}


@Preview(showBackground = true)
@Composable
fun LoggedOutPreview() {
    LoginScreenContent(
        loginState = LoginState.LoggedOut,
        performLogin = {}
    )
}

@Preview(showBackground = true)
@Composable
fun LoggingInPreview() {
    LoginScreenContent(
        loginState = LoginState.LoginInProgress,
        performLogin = {}
    )
}

@Preview(showBackground = true)
@Composable
fun LoginFailedPreview() {
    LoginScreenContent(
        loginState = LoginState.LoginFailure,
        performLogin = {}
    )
}

@Preview(showBackground = true)
@Composable
fun LoginSuccessPreview() {
    LoginScreenContent(
        loginState = LoginState.LoginSuccess,
        performLogin = {}
    )
}
