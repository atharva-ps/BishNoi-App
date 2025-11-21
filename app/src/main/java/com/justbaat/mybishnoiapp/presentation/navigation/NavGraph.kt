package com.justbaat.mybishnoiapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.justbaat.mybishnoiapp.presentation.screens.auth.login.LoginScreen
import com.justbaat.mybishnoiapp.presentation.screens.auth.register.RegisterScreen
import com.justbaat.mybishnoiapp.presentation.screens.home.HomeScreen
import com.justbaat.mybishnoiapp.presentation.screens.profile.ProfileScreen
import com.justbaat.mybishnoiapp.presentation.screens.profile.EditProfileScreen
import com.justbaat.mybishnoiapp.presentation.screens.search.SearchScreen
import com.justbaat.mybishnoiapp.presentation.screens.settings.SettingsScreen
import com.justbaat.mybishnoiapp.utils.TokenManager

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    tokenManager: TokenManager
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Navigation Graph
        navigation(
            startDestination = Screen.Login.route,
            route = Screen.AuthGraph.route
        ) {
            composable(route = Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.MainGraph.route) {
                            popUpTo(Screen.AuthGraph.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(route = Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate(Screen.MainGraph.route) {
                            popUpTo(Screen.AuthGraph.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }

        // Main Navigation Graph
        navigation(
            startDestination = Screen.Home.route,
            route = Screen.MainGraph.route
        ) {
            composable(route = Screen.Home.route) {
                HomeScreen(
                    onLogout = {
                        navController.navigate(Screen.AuthGraph.route) {
                            popUpTo(Screen.MainGraph.route) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToProfile = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }
            // In MainGraph, add Search screen:
            composable(route = Screen.Search.route) {
                SearchScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToProfile = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    }
                )
            }

            // Profile Screen
            // Profile Screen
            composable(
                route = Screen.Profile.route,
                arguments = listOf(
                    navArgument("userId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val currentUserId = tokenManager.getUserId() ?: ""

                ProfileScreen(
                    userId = userId,
                    isOwnProfile = userId == currentUserId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.createRoute(userId))
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToHome = { // ✅ Add this
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }




            // Settings Screen
            composable(route = Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onLogout = { // ✅ Add logout callback
                        navController.navigate(Screen.AuthGraph.route) {
                            popUpTo(Screen.MainGraph.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }



            // ✅ Edit Profile Screen
            composable(
                route = Screen.EditProfile.route,
                arguments = listOf(
                    navArgument("userId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                EditProfileScreen(
                    userId = userId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
