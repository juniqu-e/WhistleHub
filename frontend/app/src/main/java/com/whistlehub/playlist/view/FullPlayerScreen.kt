package com.whistlehub.playlist.view

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Pretendard
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.view.component.PlayerComment
import com.whistlehub.playlist.view.component.PlayerPlaylist
import com.whistlehub.playlist.viewmodel.PlayerViewState
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import java.util.concurrent.TimeUnit

@Composable
fun FullPlayerScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { PlayerHeader(navController) },
        bottomBar = {
            Column(Modifier.padding(bottom = paddingValues.calculateBottomPadding())) {
                PlayerController(trackPlayViewModel)
            }
        },
    ) { innerPadding ->
        // 배경 이미지
        val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
        val playerViewState by trackPlayViewModel.playerViewState.collectAsState(initial = PlayerViewState.PLAYING)

        PlayerBackground(Modifier.fillMaxSize().padding(innerPadding)
            .clickable{
            // 배경 클릭 시 트랙 재생/일시정지
            if (currentTrack != null && playerViewState == PlayerViewState.PLAYING) {
                if (trackPlayViewModel.isPlaying.value) {
                    trackPlayViewModel.pauseTrack()
                } else {
                    trackPlayViewModel.playTrack(currentTrack!!)
                }
            }
        })
        Column(Modifier.fillMaxSize().padding(innerPadding).background(CustomColors().Grey700.copy(alpha = 0.3f)), verticalArrangement = Arrangement.SpaceBetween) {
            when (playerViewState) {
                PlayerViewState.PLAYING -> {
                    TrackInfomation(Modifier.weight(1f))
                }
                PlayerViewState.PLAYLIST -> {
                    PlayerPlaylist(Modifier.weight(1f))
                }
                PlayerViewState.COMMENT -> {
                    PlayerComment(Modifier.weight(1f))
                }
                else -> Spacer(Modifier)
            }
            TrackInteraction(trackPlayViewModel)
        }
    }
}

@Composable
fun PlayerHeader(navController: NavController, trackPlayViewModel: TrackPlayViewModel = hiltViewModel()) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        IconButton(
            {
                trackPlayViewModel.setPlayerViewState(PlayerViewState.PLAYING)
                navController.navigateUp()
            },
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "뒤로가기", tint = CustomColors().Grey200)
        }
        IconButton({}) {
            Icon(Icons.Rounded.Menu, contentDescription = "더보기", tint = CustomColors().Grey200)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackInfomation(modifier: Modifier = Modifier, trackPlayViewModel: TrackPlayViewModel = hiltViewModel()) {
    // 트랙 정보를 표시하는 UI
    // 예: AsyncImage를 사용하여 이미지 로드
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    Column(modifier.background(CustomColors().Grey950.copy(alpha = 0.7f)), verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom)) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = currentTrack?.title ?: "Track Title",
            style = Typography.displaySmall,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Bold,
            color = CustomColors().Grey50,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = currentTrack?.artist?.nickname ?: "Artist Name",
            style = Typography.bodyLarge,
            color = CustomColors().Grey200,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Tags",
            style = Typography.titleMedium,
            color = CustomColors().Grey200,
            textAlign = TextAlign.Center
        )
        if (currentTrack?.tags != null) {
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
                currentTrack?.tags?.forEach { tag ->
                    Button({}) {
                        Text(
                            text = tag,
                            style = Typography.bodyLarge,
                            color = CustomColors().Grey200,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "태그가 없습니다.",
                style = Typography.bodyLarge,
                color = CustomColors().Grey200,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TrackInteraction(trackPlayViewModel: TrackPlayViewModel = hiltViewModel()) {
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    val playerViewState by trackPlayViewModel.playerViewState.collectAsState(initial = PlayerViewState.PLAYING)

    Row(Modifier.fillMaxWidth().background(CustomColors().Grey950.copy(alpha = 0.7f)), horizontalArrangement = Arrangement.SpaceBetween) {
        if (currentTrack?.isLike == true) {
            IconButton({}) {
                Icon(Icons.Filled.Favorite, contentDescription = "좋아요", tint = CustomColors().Mint500)
            }
        } else {
            IconButton({}) {
                Icon(Icons.Filled.FavoriteBorder, contentDescription = "좋아요", tint = CustomColors().Grey200)
            }
        }
        IconButton({
            if (playerViewState != PlayerViewState.COMMENT) {
                trackPlayViewModel.setPlayerViewState(PlayerViewState.COMMENT)
            } else {
                trackPlayViewModel.setPlayerViewState(PlayerViewState.PLAYING)
            }
        }) {
            // 화면에 따라 색상 전환
            Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = "댓글", tint = if (playerViewState == PlayerViewState.COMMENT) CustomColors().Mint500 else CustomColors().Grey200 )
        }
        IconButton({
            if (playerViewState != PlayerViewState.PLAYLIST) {
                trackPlayViewModel.setPlayerViewState(PlayerViewState.PLAYLIST)
            } else {
                trackPlayViewModel.setPlayerViewState(PlayerViewState.PLAYING)
            }
        }) {
            Icon(Icons.AutoMirrored.Rounded.List, contentDescription = "플레이리스트", tint = if(playerViewState == PlayerViewState.PLAYLIST) CustomColors().Mint500 else CustomColors().Grey200)
        }
    }
}

@Composable
fun PlayerBackground(modifier: Modifier = Modifier, trackPlayViewModel: TrackPlayViewModel = hiltViewModel()) {
    // 트랙의 배경 이미지를 표시하는 UI
    // 예: AsyncImage를 사용하여 이미지 로드
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    if (currentTrack?.imageUrl != null) {
        AsyncImage(
            model = currentTrack!!.imageUrl,
            contentDescription = "Track Image",
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        // 기본 배경 이미지
        Image(painterResource(R.drawable.default_track), contentDescription = "Track Image", modifier = modifier, contentScale = ContentScale.Crop)
    }
}

@Composable
fun PlayerController(
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
) {
    // 트랙 정보를 가져오기 위해 ViewModel 사용
    // 트랙 재생/일시정지/정지 버튼 클릭 시 ViewModel을 통해 트랙 제어
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    val isPlaying by trackPlayViewModel.isPlaying.collectAsState(initial = false)
    val playerPosition by trackPlayViewModel.playerPosition.collectAsState()
    val trackDuration by trackPlayViewModel.trackDuration.collectAsState()

    Column(Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Slider(
            value = playerPosition.toFloat(),
            onValueChange = { newPosition ->
                trackPlayViewModel.seekTo(newPosition.toLong())
            },
            valueRange = 0f..trackDuration.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatDuration(playerPosition), color = Color.White)
            Text(text = formatDuration(trackDuration), color = Color.White)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween)
        {
            IconButton(onClick = {
                    trackPlayViewModel.previousTrack()
                }) {
                    Icon(
                        imageVector = Icons.Rounded.FastRewind,
                        contentDescription = "PlayBack",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            trackPlayViewModel.pauseTrack()
                        } else {
                            if (currentTrack == null && trackPlayViewModel.playerTrackList.value.isNotEmpty()) {
                                // 트랙이 없을 경우 첫 번째 트랙 재생
                                trackPlayViewModel.playTrack(trackPlayViewModel.playerTrackList.value[0])
                            } else if (currentTrack != null) {
                                trackPlayViewModel.resumeTrack()
                            }
                        }
                    }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = CustomColors().Mint500
                    )
                }
                IconButton(onClick = {
                    trackPlayViewModel.nextTrack()
                }) {
                    Icon(
                        imageVector = Icons.Rounded.FastForward,
                        contentDescription = "PlayForward",
                        tint = Color.White
                    )
                }
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatDuration(durationMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
    return String.format("%02d:%02d", minutes, seconds)
}