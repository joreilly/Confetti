package dev.johnoreilly.kikiconf.android.speakers

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import dev.johnoreilly.kikiconf.android.KikiConfViewModel
import dev.johnoreilly.kikiconf.fragment.SpeakerDetails


@Composable
fun SpeakerListView(viewModel: KikiConfViewModel, bottomBar: @Composable () -> Unit) {
    val speakers by viewModel.speakers.collectAsState(emptyList())

    Scaffold(
        topBar = { TopAppBar (title = { Text("Speakers") } ) },
        bottomBar = bottomBar
    ) {
        if (speakers.isNotEmpty()) {
            LazyColumn {
                items(speakers) { speaker ->
                    SpeakerView(speaker)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
                CircularProgressIndicator()
            }
        }
    }
}


@OptIn(ExperimentalCoilApi::class)
@Composable
fun SpeakerView(speaker: SpeakerDetails) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = {  })
        .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (speaker.photoUrl.isNotEmpty()) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
            ) {
                val speakerUrl = imageUrl(speaker.photoUrl)
                Image(painter = rememberImagePainter(speakerUrl),
                    modifier = Modifier.size(60.dp),
                    contentDescription = speaker.name
                )
            }
        } else {
            Spacer(modifier = Modifier.size(60.dp))
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column {
            Text(text = speaker.name, style = TextStyle(fontSize = 20.sp))
            Text(text = speaker.company ?: "", style = TextStyle(color = Color.DarkGray, fontSize = 14.sp))
        }
    }

    Divider()
}

internal fun imageUrl(relativeUrl: String): String {
    return "https://raw.githubusercontent.com/paug/android-makers-2022/main/$relativeUrl"
        .replace("..", "")
        .replace(".svg", ".svg.png")
        .let {
            if (it.endsWith(".svg.png")) {
                it.replace("logos/", "logos/pngs/")
            } else {
                it
            }
        }

}