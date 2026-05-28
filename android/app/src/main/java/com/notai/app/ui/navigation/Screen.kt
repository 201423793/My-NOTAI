package com.notai.app.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Profile : Screen("profile")
    data object Processing : Screen("processing/{fileUri}/{platform}") {
        fun createRoute(fileUri: String, platform: String) =
            "processing/${Uri.encode(fileUri)}/$platform"
    }
    data object Result : Screen("result/{historyId}") {
        fun createRoute(historyId: Long) = "result/$historyId"
    }
}
