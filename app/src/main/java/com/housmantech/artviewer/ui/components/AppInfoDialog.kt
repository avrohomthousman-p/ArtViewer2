package com.housmantech.artviewer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.housmantech.artviewer.R


/**
 * Information dialog describing the basic purpose of the app
 */
@Composable
fun AppInfoDialog(dismissDialog: () -> Unit) {
    Dialog(onDismissRequest = { dismissDialog() }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.dialog_app_info_title),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))


                Text(
                    text = stringResource(R.string.dialog_app_info_problem_statement),
                    fontSize = 18.sp
                )


                Spacer(Modifier.height(40.dp))

                StandardButton(
                    modifier = Modifier,
                    onClick = { dismissDialog() },
                    text = stringResource(R.string.got_it)
                )
            }
        }
    }
}