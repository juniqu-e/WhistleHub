package com.whistlehub

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.whistlehub.common.view.WhistleHubNavHost
import com.whistlehub.common.view.WhistleHubNavigation
import com.whistlehub.common.view.navigation.Screen
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
                //현재 Navigation 탐색
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry.value?.destination?.route
                val context = LocalContext.current
                val activity = context as? Activity

                LaunchedEffect(currentRoute) {
                    if (currentRoute == Screen.DAW.route) {
                        activity?.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        //세로 고정
                        // [You should not lock orientation of your activities, so that you can support a good user experience for any device or orientation]
                        // Suggest Ignore
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }

                Scaffold(modifier = Modifier
                    .fillMaxSize()
                    .then(if (currentRoute != Screen.DAW.route) Modifier.systemBarsPadding() else Modifier),
                    bottomBar = {
                        WhistleHubNavigation(navController = navController)
                    },
                    content = { paddingValues ->
                        val cur =
                            navController.currentBackStackEntryAsState().value?.destination?.route
                        val appliedModifier =
                            if (cur == Screen.DAW.route) {
                                Modifier.padding(0.dp)
                            } else {
                                Modifier.padding(paddingValues)
                            }
                        WhistleHubNavHost(
                            navController = navController,
                            modifier = appliedModifier
                        )
                    })
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