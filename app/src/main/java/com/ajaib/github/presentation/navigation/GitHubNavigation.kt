package com.ajaib.github.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ajaib.github.presentation.screens.UserDetailScreen
import com.ajaib.github.presentation.screens.UserListScreen
import com.ajaib.github.presentation.screens.UserRepositoriesScreen

@Composable
fun GitHubNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.UserList.route
    ) {
        composable(Screen.UserList.route) {
            UserListScreen(
                onNavigateToUserDetail = { username ->
                    navController.navigate(Screen.UserDetail.createRoute(username))
                }
            )
        }

        composable(Screen.UserDetail.route) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            UserDetailScreen(
                username = username,
                onNavigateToRepositories = { username ->
                    navController.navigate(Screen.UserRepositories.createRoute(username))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.UserRepositories.route) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            UserRepositoriesScreen(
                username = username,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}