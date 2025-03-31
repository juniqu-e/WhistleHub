package com.whistlehub

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.navigation.LogoutHandler
import com.whistlehub.common.view.navigation.MainNavGraph
import com.whistlehub.common.view.theme.WhistleHubTheme
import com.whistlehub.common.view.typography.Pretendard
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var logoutManager: LogoutManager

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
                LogoutHandler(
                    navController = navController,
                    logoutManager = logoutManager  // 또는 loginViewModel.logoutEventFlow
                )
                // 메인 네비게이션 그래프만 실행
                MainNavGraph(
                    navController = navController,
                    logoutManager = logoutManager,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}