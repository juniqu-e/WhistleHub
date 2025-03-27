package com.whistlehub.common.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.whistlehub.common.view.AppScaffold
import com.whistlehub.common.view.login.LoginScreen
import com.whistlehub.common.view.signup.SelectTagsScreen
import com.whistlehub.common.view.signup.SignUpScreen
import com.whistlehub.common.viewmodel.SignUpViewModel

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
                    // 로그인 성공 시 메인 화면으로 이동
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignUpClick = {
                    // 회원가입 화면으로 이동
                    navController.navigate("signup") {
                        popUpTo("login") { inclusive = true }
                    }

                }
            )
        }
        // 회원가입 정보 화면
        composable("signup") {
            SignUpScreen(
                onNext = {
                    // 태그 선택 화면으로 전환
                    userId, password, nickname, email, gender, birth ->
                    navController.navigate("selectTags/$userId/$password/$nickname/$email/$gender/$birth")
                },
                onLoginClick = {
                    // 로그인 화면으로 이동
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "select_tags?userId={userId}&password={password}&email={email}&nickname={nickname}&gender={gender}&birth={birth}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType },
                navArgument("nickname") { type = NavType.StringType },
                navArgument("gender") { type = NavType.StringType },
                navArgument("birth") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val password = backStackEntry.arguments?.getString("password") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val nickname = backStackEntry.arguments?.getString("nickname") ?: ""
            val genderString = backStackEntry.arguments?.getString("gender") ?: "M"
            val birth = backStackEntry.arguments?.getString("birth") ?: ""

            // Composable 컨텍스트에서 미리 ViewModel 인스턴스를 생성합니다.
            val signUpViewModel: SignUpViewModel = hiltViewModel()

            SelectTagsScreen(
                userId = userId,
                password = password,
                email = email,
                nickname = nickname,
                gender = genderString.first(), // genderString는 "M" 또는 "F"
                birth = birth,
                onStartClick = { selectedTags ->
                    // 태그 선택 후 회원가입 API 호출
                    signUpViewModel.register(
                        loginId = userId,
                        password = password,
                        email = email,
                        nickname = nickname,
                        birth = birth,
                        gender = genderString.first(),
                        tags = selectedTags
                    ) {
                        // 회원가입 성공 시 메인 화면으로 이동
                        navController.navigate("main") {
                            popUpTo("signup") { inclusive = true }
                        }
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
