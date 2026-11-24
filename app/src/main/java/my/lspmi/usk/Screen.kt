package my.lspmi.usk

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import my.lspmi.usk.menus.HomeScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")

    object Menu : Screen("menu/{username}") {
        fun createRoute(username: String): String {
            return "menu/${Uri.encode(username)}"
        }
    }
}

@Composable
fun AppNavigation () {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(
            route = Screen.Login.route
        ) {
            LoginScreen(navController = navController)
        }
        composable(
            route = Screen.Register.route
        ) {
            RegisterScreen(navController = navController)
        }
        composable(
            route = Screen.Menu.route,
            arguments = listOf(navArgument("username") { type = NavType.StringType }),
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            MainScreen(navController = navController, username = username)
        }
    }
}