package com.justbaat.mybishnoiapp.presentation.navigation

sealed class Screen(val route: String) {
    // Auth Routes
    object Login : Screen("login")
    object Register : Screen("register")

    // Main Routes
    object Home : Screen("home")
    object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    object EditProfile : Screen("edit_profile/{userId}") { // ✅ Updated with userId parameter
        fun createRoute(userId: String) = "edit_profile/$userId"
    }
    object Feed : Screen("feed")
    object Community : Screen("community")

    // Graph Routes
    object AuthGraph : Screen("auth_graph")
    object MainGraph : Screen("main_graph")

    object Settings : Screen("settings")
}

