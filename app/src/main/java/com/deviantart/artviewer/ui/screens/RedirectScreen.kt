package com.deviantart.artviewer.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout


/**
 * Screen for the redirect activity
 */
@Composable
fun RedirectScreen() {

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {

        val (toolbar, title, status) = createRefs()


        Text(text = "This activity has not been set up yet", modifier = Modifier.constrainAs(title) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        })
        //TODO: real content
    }
}