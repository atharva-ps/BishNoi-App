package com.app.bishnoi.presentation.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    // Auth Routes
    object Login : Screen("login")
    object Register : Screen("register")

    // Main Routes
    object Home : Screen("home")
    object Search : Screen("search") // ✅ Add this
    object Notifications : Screen("notifications")
    object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    object EditProfile : Screen("edit_profile/{userId}") {
        fun createRoute(userId: String) = "edit_profile/$userId"
    }
    object Settings : Screen("settings")

    // Graph Routes
    object AuthGraph : Screen("auth_graph")
    object MainGraph : Screen("main_graph")

    object Followers : Screen("followers/{userId}") {
        fun createRoute(userId: String) = "followers/$userId"
    }
    object Following : Screen("following/{userId}") {
        fun createRoute(userId: String) = "following/$userId"
    }
    object CreatePost : Screen("create_post")

    object PostDetail : Screen("post_detail")

    object Members : Screen("members")

    object ForgotPassword : Screen("forgot_password")

    object News : Screen("news")

    object Social : Screen("social")

    // ✅ WebView with default title
    object WebView : Screen("webview/{url}/{title}") {
        fun createRoute(url: String, title: String = "News"): String {  // ✅ Add default value
            val encodedUrl = Uri.encode(url)  // ✅ Use Uri.encode instead
            val encodedTitle = Uri.encode(title)
            return "webview/$encodedUrl/$encodedTitle"
        }
    }
}
