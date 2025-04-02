package com.whistlehub.workstation.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.view.theme.Typography

@Composable
fun SearchLayerSection(
    searchResults: List<TrackResponse.SearchTrack>,
    onSearchClicked: (String) -> Unit,
    onTrackSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    var keyword by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "음원 검색",
            style = Typography.titleLarge,
            color = Color.Black,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(Modifier.height(8.dp))

        TextField(
            value = keyword,
            onValueChange = { keyword = it },
            label = { Text("검색어 입력") }
        )

        Button(onClick = {
            onSearchClicked(keyword)
        }) {
            Text("검색")
        }



        Spacer(Modifier.height(16.dp))

        searchResults.forEach { track ->
            Button(onClick = {
                onTrackSelected(track.trackId)
            }) {
                Text(track.nickname)
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF0F0F0),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null
            )
            Spacer(Modifier.width(4.dp))
            Text("이전", style = Typography.bodyLarge)
        }
        Spacer(Modifier.width(16.dp))
    }
}

