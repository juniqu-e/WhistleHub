package com.whistlehub.common.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.SharedFlow

/**
 * LogOutHandler는 UI 계층에서 중앙 집중식으로 로그아웃 이벤트를 구독하는 Composable입니다.
 *
 * 주요 특징:
 * - LogoutManager를 통해 노출된 SharedFlow<Unit> 형태의 로그아웃 이벤트를 구독합니다.
 * - 현재 네비게이션 스택의 최상위 라우트를 확인하여,
 * - 이미 로그인, 회원가입, 또는 비밀번호 재설정 화면일 때는 네비게이트하지 않고
 * - 토큰이 사라지는 등 로그아웃 이벤트가 발생하면 로그인 화면("login")으로 전환
 *
 * 사용 방법:
 * - MainNavGraph 또는 최상위 Composable에 LogOutHandler를 포함시켜, 한 번만 로그아웃 이벤트를 구독하도록 합니다.
 * - 이로써 앱 전체에서 로그아웃 이벤트가 발생할 때 한 곳에서 중앙 집중식으로 네비게이션 전환 로직을 처리할 수 있습니다.
 *
 * @param navController 네비게이션 컨트롤러. 로그아웃 이벤트 발생 시 이를 사용해 화면 전환을 수행합니다.
 * @param logoutEventFlow 로그아웃 이벤트가 발생하는 SharedFlow입니다. 보통 LogoutManager 또는 LoginViewModel에서 제공됩니다.
 */

@Composable
fun LogoutHandler(
    navController: NavHostController,
    logoutManager: LogoutManager
) {
    LaunchedEffect(key1 = logoutManager.logoutEventFlow) {
        logoutManager.logoutEventFlow.collect {
            android.util.Log.d("LogoutHandler", "LogoutHandler LaunchedEffect started")
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            android.util.Log.d("LogoutHandler", "Logout event received. Current route: $currentRoute")
            if (currentRoute !in listOf("login", "signup", "selecttags", "passwordreset")) {
                navController.navigate("login") {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        }
    }
}
