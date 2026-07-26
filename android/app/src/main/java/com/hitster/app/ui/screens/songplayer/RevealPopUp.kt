package com.hitster.app.ui.screens.songplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hitster.app.ui.theme.Purple80

@Composable
fun RevealPopUp(
    year: Int,
    artist: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        color = Purple80,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = year.toString(),
                style = TextStyle(
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = TextStyle(
                    fontSize = 26.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = artist,
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
@Composable
fun RevealPopUpPreview() {
    RevealPopUp(year = 2017, artist = "Imagine A lot of really scary really big really really really long Dragons", title = "Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa veeery very very very very veeeeeeery long song title ")
}


