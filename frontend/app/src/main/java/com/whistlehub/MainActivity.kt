package com.whistlehub

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.whistlehub.common.view.WhistleHubNavHost
import com.whistlehub.common.view.WhistleHubNavigation
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.view.theme.WhistleHubTheme
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
                Scaffold(modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                    bottomBar = {
                        WhistleHubNavigation(navController = navController)
                    },
                    content = { paddingValues ->
                        WhistleHubNavHost(
                            navController = navController,
                            modifier = Modifier.padding(paddingValues)
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
    Column {
        Text(text = name, style = Typography.titleLarge)
    }
}

@Composable
fun BodyContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = "Hi there!", style = Typography.titleLarge)
        Text(text = "Welcome To Sucking WhistleHub", style = Typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WhistleHubTheme {
        Greeting("Android")
    }
}