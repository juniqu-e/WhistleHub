package com.whistlehub.common.view.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.R
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Pretendard
import com.whistlehub.common.viewmodel.LoginSplashViewModel

@Composable
fun LoginSplashScreen(
    navController: NavHostController,
    splashViewModel: LoginSplashViewModel = hiltViewModel()
) {
    val isLoggedIn by splashViewModel.isLoggedIn.collectAsState()
    val customColors = CustomColors()

    // 애니메이션 상태 관리
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    // 로고 스케일 애니메이션
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoScale"
    )

    // 로딩 인디케이터 회전 애니메이션
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // 로그인 상태에 따른 화면 이동
    LaunchedEffect(isLoggedIn) {
        when (isLoggedIn) {
            true -> {
                // 자동 로그인 성공 → 메인 화면 이동
                navController.navigate("main") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            false -> {
                // 자동 로그인 실패 → 로그인 화면 이동
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            null -> {
                // 아직 로딩 중
            }
        }
    }

    // 로딩 화면 표시
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(customColors.CommonBackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 로고 (스케일 애니메이션 적용)
            Image(
                painter = painterResource(id = R.drawable.whistlehub_mainlogo),
                contentDescription = "Whistle Hub Logo",
                modifier = Modifier
                    .size(200.dp)
                    .scale(logoScale)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 커스텀 크기의 로딩 인디케이터
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                color = customColors.CommonButtonColor,
                strokeWidth = 5.dp
            )
        }
    }
}