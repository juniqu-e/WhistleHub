package com.whistlehub.common.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.whistlehub.common.view.AppScaffold
import com.whistlehub.common.view.login.LoginScreen

/**
 * 앱의 전체 네비게이션 구조를 처리하는 메인 네비게이션 그래프
 */
@Composable
fun MainNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        //유저 인증(로그인)
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // 로그인 성공 시, 로그인 화면 제거 후 메인 화면으로 전환
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        //휘슬허브 메인 화면들
        composable("main") {
            MainScreenWithBottomNav(navController)
        }
    }
}

/**
 * 메인 앱 콘텐츠와 하단 네비게이션이 포함된 화면
 */
@Composable
fun MainScreenWithBottomNav(navController: NavHostController) {
    // 새로운 내부 네비게이션 컨트롤러 생성
    val newNavController = rememberNavController()
    AppScaffold(
        navController = newNavController,
        bottomBar = {
            BottomNavigationBar(navController = newNavController)
        },
    ) { paddingValues ->
        AppContentNavGraph(
            navController = newNavController,
            paddingValues = paddingValues,
            originNavController = navController // 기존 컨트롤러는 로그아웃 시 사용하기 위해 전달
        )
    }
}
