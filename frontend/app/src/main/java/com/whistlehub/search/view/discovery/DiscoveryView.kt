package com.whistlehub.search.view.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        columns = GridCells.Adaptive(minSize = 150.dp)
    ) {
        items(tags.size) { index ->
            val tag = tags[index]
            Box(
                Modifier
                    .padding(10.dp)
                    .background(CustomColors().Mint500, RoundedCornerShape(10.dp))
                    .padding(top = 70.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
                    .clickable {
                        coroutineScope.launch {
                            searchViewModel.getRankingByTag(tag.id, "WEEK")
                            navController.navigate(Screen.TagRanking.route + "/${tag.id}/${tag.name}")
                        }
                    },
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = "#${tag.name}",
                    modifier = Modifier
                        .padding(5.dp),
                    style = Typography.bodyMedium,
                    color = CustomColors().Grey950
                )
            }
        }
    }
}