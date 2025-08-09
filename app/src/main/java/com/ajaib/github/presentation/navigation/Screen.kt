package com.ajaib.github.presentation.navigation

sealed class Screen(val route: String) {
    object UserList : Screen("user_list")
    object UserDetail : Screen("user_detail/{username}") {
        fun createRoute(username: String) = "user_detail/$username"
    }
    object UserRepositories : Screen("user_repositories/{username}") {
        fun createRoute(username: String) = "user_repositories/$username"
    }
}