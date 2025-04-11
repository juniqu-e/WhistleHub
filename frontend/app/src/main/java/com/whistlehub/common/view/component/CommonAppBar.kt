package com.whistlehub.common.view.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import kotlinx.coroutines.CoroutineScope

/**
 * 앱 전체에서 공통으로 사용하는 상단 앱바 컴포넌트
 *
 * @param title 앱바에 표시할 제목
 * @param navController 화면 이동을 위한 네비게이션 컨트롤러
 * @param logoutManager 로그아웃 관리자
 * @param coroutineScope 코루틴 스코프
 * @param showBackButton 뒤로가기 버튼 표시 여부
 * @param showMenuButton 메뉴 버튼 표시 여부
 * @param onBackClick 뒤로가기 버튼 클릭 시 실행할 추가 동작 (null이면 기본 동작만 실행)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonAppBar(
    title: String,
    navController: NavHostController,
    logoutManager: LogoutManager,
    coroutineScope: CoroutineScope,
    showBackButton: Boolean = false,
    showMenuButton: Boolean = true,
    onBackClick: (() -> Unit)? = null
) {
    val customColors = CustomColors()

    TopAppBar(
        title = {
            Text(
                text = title,
                style = Typography.titleLarge,
                color = customColors.Grey50,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = {
                    onBackClick?.invoke() ?: navController.popBackStack()
                }) {
                    // AutoMirrored 버전의 ArrowBack 아이콘으로 변경
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = customColors.Grey50
                    )
                }
            }
        },
        actions = {
            if (showMenuButton) {
                IconButton(onClick = {
                    navController.navigate(Screen.ProfileMenu.route)
                }) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "메뉴",
                        tint = customColors.Grey50
                    )
                }
            }
        }
    )
}