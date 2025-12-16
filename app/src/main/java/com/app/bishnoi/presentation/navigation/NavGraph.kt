package com.app.bishnoi.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.app.bishnoi.domain.model.Post
import com.app.bishnoi.presentation.screens.auth.login.LoginScreen
import com.app.bishnoi.presentation.screens.auth.register.RegisterScreen
import com.app.bishnoi.presentation.screens.createpost.CreatePostScreen
import com.app.bishnoi.presentation.screens.follow.FollowersScreen
import com.app.bishnoi.presentation.screens.follow.FollowingScreen
import com.app.bishnoi.presentation.screens.home.HomeScreen
import com.app.bishnoi.presentation.screens.home.HomeViewModel
import com.app.bishnoi.presentation.screens.members.MembersScreen
import com.app.bishnoi.presentation.screens.postdetail.PostDetailScreen
import com.app.bishnoi.presentation.screens.profile.ProfileScreen
import com.app.bishnoi.presentation.screens.profile.EditProfileScreen
import com.app.bishnoi.presentation.screens.search.SearchScreen
import com.app.bishnoi.presentation.screens.settings.SettingsScreen
import com.app.bishnoi.utils.TokenManager

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
                    },
                    onNavigateToCreatePost = {
                        navController.navigate(Screen.CreatePost.route)   // ✅ navigate to create post
                    },
                    onNavigateToPostDetail = { post ->  // ✅ Add this
                        val postJson = Gson().toJson(post)
                        val encodedJson = java.net.URLEncoder.encode(postJson, "UTF-8")
                        navController.navigate("${Screen.PostDetail.route}/$encodedJson")
                    },
                    onNavigateToMembers = {  // ✅ Add this
                        navController.navigate(Screen.Members.route)
                    }
                )
            }

            // In MainGraph, add Search screen:
            composable(route = Screen.Search.route) {
                SearchScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToMembers = {  // ✅ Add this
                        navController.navigate(Screen.Members.route)
                    },
                    onNavigateToProfile = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    }
                )
            }

            // Profile Screen
            composable(
                route = Screen.Profile.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val currentUserId = tokenManager.getUserId() ?: ""

                ProfileScreen(
                    userId = userId,
                    isOwnProfile = userId == currentUserId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.createRoute(userId))
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToFollowers = { userId ->
                        navController.navigate(Screen.Followers.createRoute(userId))
                    },
                    onNavigateToFollowing = { userId ->
                        navController.navigate(Screen.Following.createRoute(userId))
                    },
                    onNavigateToMembers = {  // ✅ Add
                        navController.navigate(Screen.Members.route)
                    },
                    onNavigateToCreatePost = {  // ✅ Add
                        navController.navigate(Screen.CreatePost.route)
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

            // ✅ Members Screen
            composable(route = Screen.Members.route) {
                MembersScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToCreatePost = {
                        navController.navigate(Screen.CreatePost.route)
                    },
                    onNavigateToProfile = { userId ->
                        if (userId.isEmpty()) {
                            // Navigate to own profile
                            val currentUserId = tokenManager.getUserId() ?: ""
                            navController.navigate(Screen.Profile.createRoute(currentUserId))
                        } else {
                            navController.navigate(Screen.Profile.createRoute(userId))
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

            // Followers screen
            composable(
                route = Screen.Followers.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                FollowersScreen(
                    userId = userId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProfile = { profileUserId ->
                        navController.navigate(Screen.Profile.createRoute(profileUserId))
                    }
                )
            }

            // Following screen
            composable(
                route = Screen.Following.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                FollowingScreen(
                    userId = userId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProfile = { profileUserId ->
                        navController.navigate(Screen.Profile.createRoute(profileUserId))
                    }
                )
            }
            composable(Screen.CreatePost.route) {
                CreatePostScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onPostCreated = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = "${Screen.PostDetail.route}/{postJson}",
                arguments = listOf(
                    navArgument("postJson") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val postJson = backStackEntry.arguments?.getString("postJson") ?: ""
                val post = try {
                    Gson().fromJson(postJson, Post::class.java)
                } catch (e: Exception) {
                    null
                }

                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.Home.route)
                }
                val homeViewModel: HomeViewModel = hiltViewModel(parentEntry)

                // ✅ Fix: Collect state properly instead of using .value
                val homeUiState by homeViewModel.uiState.collectAsState()

                if (post != null) {
                    PostDetailScreen(
                        post = post,
                        currentUserId = homeUiState.currentUser?.id,  // ✅ Use collected state
                        onNavigateBack = { updatedPost ->  // ✅ Receive updated post
                            homeViewModel.updatePost(updatedPost)  // ✅ Update in HomeViewModel
                            navController.popBackStack()
                        },
                        onNavigateToProfile = { userId ->
                            navController.navigate(Screen.Profile.createRoute(userId))
                        },
                        onDeletePost = {
                            homeViewModel.deletePost(post.id)
                        }
                    )
                }
            }
        }
    }
}
