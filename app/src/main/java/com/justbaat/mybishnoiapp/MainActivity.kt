package com.justbaat.mybishnoiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.justbaat.mybishnoiapp.presentation.navigation.NavGraph
import com.justbaat.mybishnoiapp.presentation.navigation.Screen
import com.justbaat.mybishnoiapp.ui.theme.BishNoiTheme
import com.justbaat.mybishnoiapp.utils.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BishNoiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val isLoggedIn = remember { tokenManager.isLoggedIn() }

                    val startDestination = if (isLoggedIn) {
                        Screen.MainGraph.route
                    } else {
                        Screen.AuthGraph.route
                    }

                    NavGraph(
                        navController = navController,
                        startDestination = startDestination,
                        tokenManager = tokenManager
                    )
                }
            }
        }
    }
}
