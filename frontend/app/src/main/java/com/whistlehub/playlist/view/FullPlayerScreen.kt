package com.whistlehub.playlist.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        val playerViewState by trackPlayViewModel.playerViewState.collectAsState(initial = PlayerViewState.PLAYING)

        PlayerBackground(Modifier.fillMaxSize().padding(innerPadding)
            .clickable{
            // 배경 클릭 시 트랙 재생/일시정지
            if (currentTrack != null && playerViewState == PlayerViewState.PLAYING) {
                if (trackPlayViewModel.isPlaying.value) {
                    trackPlayViewModel.pauseTrack()
                } else {
                    trackPlayViewModel.resumeTrack()
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
            text = currentTrack?.artistInfo?.nickname ?: "Artist Name",
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
        if (currentTrack?.tags?.isNotEmpty() == true) {
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
                currentTrack?.tags?.forEach { tag ->
                    Button({}) {
                        Text(
                            text = tag.name,
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
fun TrackMenu(trackPlayViewModel: TrackPlayViewModel = hiltViewModel(), onReportClick: () -> Unit = {}) {
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    Column(modifier = Modifier.heightIn(min = 200.dp).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom),
        horizontalAlignment = Alignment.CenterHorizontally) {
        if (currentTrack?.imageUrl != null) {
            AsyncImage(
                model = currentTrack!!.imageUrl,
                contentDescription = "Track Image",
                modifier = Modifier.size(75.dp),
                contentScale = ContentScale.Crop,
            )
        } else {
            // 기본 배경 이미지
            Image(painterResource(R.drawable.default_track),
                contentDescription = "Track Image",
                modifier = Modifier.size(75.dp),
                contentScale = ContentScale.Crop)
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = currentTrack?.title ?: "Track Title",
            style = Typography.titleMedium,
            color = CustomColors().Grey50,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = currentTrack?.artistInfo?.nickname ?: "Artist Name",
            style = Typography.bodyLarge,
            color = CustomColors().Mint500,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Tags",
            style = Typography.titleSmall,
            color = CustomColors().Grey200,
            textAlign = TextAlign.Center
        )
        if (currentTrack?.tags?.isNotEmpty() == true) {
            FlowRow(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
                currentTrack?.tags?.forEach { tag ->
                    Button({}) {
                        Text(
                            text = tag.name,
                            style = Typography.bodySmall,
                            color = CustomColors().Grey950,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "태그가 없습니다.",
                style = Typography.bodySmall,
                color = CustomColors().Grey200,
                textAlign = TextAlign.Center
            )
        }
        Row(
            Modifier.clickable{}.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("플레이리스트에 추가")
            IconButton({}) {
                Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "플레이리스트에 추가",
                    tint = CustomColors().Grey200,
                    modifier = Modifier.size(16.dp))
            }
        }
        HorizontalDivider(thickness = 1.dp, color = CustomColors().Grey50)
        Row(
            Modifier.clickable{}.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("내 트랙에 Import")
            IconButton({}) {
                Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "내 트랙에 Import",
                    tint = CustomColors().Grey200,
                    modifier = Modifier.size(16.dp))
            }
        }
        if (true /* 내 트랙이 아닐 때 */) {
            HorizontalDivider(thickness = 1.dp, color = CustomColors().Grey50)
            Row(
                Modifier.clickable {
                    onReportClick()
                }.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("신고")
                IconButton({
                    onReportClick()
                }) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        contentDescription = "신고",
                        tint = CustomColors().Grey200,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
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