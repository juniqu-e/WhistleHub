import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun DAWScreen() {
    val timelineWidth = 600.dp
    val tracks = remember {
        mutableStateListOf(
            TrackData("Track 1", Color.Blue),
            TrackData("Track 2", Color.Green),
            TrackData("Track 3", Color.Yellow),
            TrackData("Track 4", Color.Magenta)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopMenu()
        TimelineAndTracks(timelineWidth, tracks)
        MixerControls()
    }
}

data class TrackData(val name: String, val color: Color, val initialOffset: Dp = 0.dp)

@Composable
fun TopMenu() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "File")
        Text(text = "Edit")
    }
}

@Composable
fun TimelineAndTracks(timelineWidth: Dp, tracks: List<TrackData>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // 타임라인 표시
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            for (i in 1..10) {
                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Text(text = "$i")
                }
            }
        }
        // 오디오 트랙들
        tracks.forEachIndexed { index, track ->
            DraggableTrack(track = track, timelineWidth = timelineWidth, trackIndex = index)
        }
    }
}

@Composable
fun DraggableTrack(track: TrackData, timelineWidth: Dp, trackIndex: Int) {
    var trackOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .offset { IntOffset(trackOffset.x.toInt(), 0) }
            .background(track.color)
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    // 이동할 범위를 타임라인 너비에 맞게 조정
                    trackOffset = Offset(
                        x = (trackOffset.x + dragAmount.x).coerceIn(0f, timelineWidth.toPx()),
                        y = trackOffset.y
                    )
                }
            }
    ) {
        Text(
            text = track.name,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(8.dp),
            color = Color.White
        )
    }
}

@Composable
fun MixerControls() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Mixer Controls")
        // 믹서 볼륨 슬라이더
        VolumeSlider("Track 1")
        VolumeSlider("Track 2")
        VolumeSlider("Track 3")
        VolumeSlider("Track 4")
        // 이펙트 슬라이더
        EffectSlider("Gain")
        EffectSlider("Frequency")
        EffectSlider("Reverb")
    }
}

@Composable
fun VolumeSlider(trackName: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = "$trackName Volume")
        Slider(value = 0.5f, onValueChange = {}, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun EffectSlider(effectName: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = effectName)
        Slider(value = 0.5f, onValueChange = {}, modifier = Modifier.fillMaxWidth())
    }
}


