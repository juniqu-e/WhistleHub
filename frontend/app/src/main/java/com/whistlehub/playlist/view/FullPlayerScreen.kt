package com.whistlehub.playlist.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel

@Composable
fun FullPlayerScreen(
    navController: NavController,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { PlayerHeader(navController) },
        bottomBar = {
            PlayerController(trackPlayViewModel)
        },
    ) { innerPadding ->
        // 배경 이미지
        val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
        PlayerBackground(trackPlayViewModel, Modifier.fillMaxSize().padding(innerPadding)
            .clickable{
            // 배경 클릭 시 트랙 재생/일시정지
            if (currentTrack != null) {
                if (trackPlayViewModel.isPlaying.value) {
                    trackPlayViewModel.pauseTrack()
                } else {
                    trackPlayViewModel.playTrack(currentTrack!!)
                }
            }
        })
        Column(Modifier.fillMaxSize().padding(innerPadding).background(CustomColors().Grey700.copy(alpha = 0.3f)), verticalArrangement = Arrangement.SpaceBetween) {
            Spacer(Modifier)
            TrackInteraction(trackPlayViewModel)
        }
    }
}

@Composable
fun PlayerHeader(navController: NavController) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        IconButton(
            {
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

@Composable
fun TrackInteraction(trackPlayViewModel: TrackPlayViewModel = hiltViewModel()) {
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        if (currentTrack?.isLike == true) {
            IconButton({}) {
                Icon(Icons.Filled.Favorite, contentDescription = "좋아요", tint = CustomColors().Mint500)
            }
        } else {
            IconButton({}) {
                Icon(Icons.Filled.FavoriteBorder, contentDescription = "좋아요", tint = CustomColors().Grey200)
            }
        }
        IconButton({}) {
            Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = "댓글", tint = CustomColors().Grey200)
        }
        IconButton({}) {
            Icon(Icons.AutoMirrored.Rounded.List, contentDescription = "플레이리스트", tint = CustomColors().Grey200)
        }
    }
}

@Composable
fun PlayerBackground(trackPlayViewModel: TrackPlayViewModel = hiltViewModel(), modifier: Modifier = Modifier) {
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

    Column(Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(CustomColors().Grey500)
                .padding(bottom = 16.dp),
        ) {
            // 현재 재생 중인 트랙의 진행 상태를 나타내는 UI
            // 예: 진행 바, 시간 표시 등
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween)
        {
            IconButton({}) {
                Icon(Icons.Filled.FastRewind, contentDescription = "이전", tint = CustomColors().Grey200)
            }
            if (isPlaying && currentTrack != null) {
                IconButton({
                    trackPlayViewModel.pauseTrack()
                }) {
                    Icon(Icons.Filled.Pause, contentDescription = "일시정지", tint = CustomColors().Mint500)
                }
            } else {
                IconButton({
                    trackPlayViewModel.playTrack(currentTrack!!)
                }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "재생/일시정지", tint = CustomColors().Mint500)
                }
            }
            IconButton({}) {
                Icon(Icons.Filled.FastForward, contentDescription = "다음", tint = CustomColors().Grey200)
            }
        }
    }

}