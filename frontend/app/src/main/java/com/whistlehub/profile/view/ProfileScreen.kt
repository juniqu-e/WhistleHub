package com.whistlehub.profile.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.common.viewmodel.LoginViewModel

@Composable
fun ProfileScreen(originNavController: NavHostController, loginViewModel: LoginViewModel = hiltViewModel()) {

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("마이페이지 화면")
        Button({
            loginViewModel.logout()
            originNavController.navigate("login") {
                popUpTo("login") { inclusive = true }
            }
        }) {
            Text("로그아웃")
        }
    }
}