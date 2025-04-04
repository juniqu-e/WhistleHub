package com.whistlehub.search.view.discovery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.common.data.remote.dto.response.AuthResponse
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.search.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

@Composable
fun DiscoveryView(
    modifier: Modifier,
    tags: List<AuthResponse.TagResponse>,
    navController: NavHostController,
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(tags.size) { index ->
            val tag = tags[index]
            val resourceName = "tag_${tag.id}"
            val context = LocalContext.current
            val resId = remember(resourceName) {
                context.resources.getIdentifier(resourceName, "drawable", context.packageName)
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.618f)
                    .clickable {
                        coroutineScope.launch {
                            searchViewModel.getRankingByTag(tag.id, "WEEK")
                            navController.navigate(Screen.TagRanking.route + "/${tag.id}/${tag.name}")
                        }
                    },
                contentAlignment = Alignment.BottomEnd
            ) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                )
                // 검은색 반투명 오버레이
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp)) // 이미지와 똑같이 자르기
                        .background(Color.Black.copy(alpha = 0.3f))
                )
                Text(
                    text = "#${tag.name}",
                    style = Typography.titleLarge,
                    color = CustomColors().Grey50,
                    modifier = Modifier.padding(bottom = 10.dp, end = 10.dp)
                )
            }
        }
    }
}