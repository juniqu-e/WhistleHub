package com.whistlehub

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.whistlehub.common.view.navigation.MainNavGraph
import com.whistlehub.common.view.theme.WhistleHubTheme
import com.whistlehub.common.view.typography.Pretendard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private external fun startAudioEngine(): Int
    private external fun stopAudioEngine(): Int

    companion object {
        init {
            System.loadLibrary("whistlehub")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhistleHubTheme {
                val navController = rememberNavController()
                // 메인 네비게이션 그래프만 실행
                MainNavGraph(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        val result = startAudioEngine()
        if (result == 0) {
            Log.d("MainActivity", "Audio engine started successfully")
        } else {
            Log.e("MainActivity", "Audio engine failed to start")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudioEngine()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val pretendard = Pretendard()

    pretendard.TitleLarge(
        text = "휘슬허브",
        modifier = Modifier
    )


}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WhistleHubTheme {
        Greeting("Android")
    }
}