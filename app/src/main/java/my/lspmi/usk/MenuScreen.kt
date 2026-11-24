package my.lspmi.usk

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import my.lspmi.usk.menus.HomeScreen
import my.lspmi.usk.menus.MapScreen
import my.lspmi.usk.menus.ProfileScreen

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    username: String
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper.getInstance(context) }
    var user by remember { mutableStateOf(dbHelper.getUserByUsername(username)) }

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
                    label = { Text("Map") }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                user = user
            )
            1 -> ProfileScreen(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                user = user,
                onUserUpdated = {
                    user = dbHelper.getUserByUsername(username)
                }
            )
            2 -> MapScreen()
        }
    }
}
