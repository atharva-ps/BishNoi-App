package com.justbaat.mybishnoiapp.presentation.navigation

sealed class Screen(val route: String) {
    // Auth Routes
    object Login : Screen("login")
    object Register : Screen("register")

    // Main Routes
    object Home : Screen("home")
    object Search : Screen("search") // ✅ Add this
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


}
