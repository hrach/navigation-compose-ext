package dev.hrach.navigation.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.hrach.navigation.bottomsheet.BottomSheetNavigator
import dev.hrach.navigation.modalsheet.ModalSheetNavigator

@Composable
public fun App() {
	MaterialTheme {
		val modalSheetNavigator = remember { ModalSheetNavigator() }
		val bottomSheetNavigator = remember { BottomSheetNavigator() }
		val navController = rememberNavController(modalSheetNavigator, bottomSheetNavigator)
		Scaffold(
			bottomBar = {
				BottomBar(navController)
			},
		) {
			Box(Modifier.padding(it)) {
				NavHost(navController, modalSheetNavigator, bottomSheetNavigator)
			}
		}
	}
}

private data class Item(
	val id: Int,
	val title: String,
	val icon: ImageVector,
	val destination: Any,
)

private val Items = listOf(
	Item(
		0,
		"Home",
		Icons.Default.Home,
		Destinations.Home,
	),
	Item(
		1,
		"List",
		Icons.AutoMirrored.Default.List,
		Destinations.List,
	),
	Item(
		2,
		"Profile",
		Icons.Default.Person,
		Destinations.Profile,
	),
)

@Composable
private fun BottomBar(navController: NavHostController) {
	val navBackStackEntry by navController.currentBackStackEntryAsState()

	@Suppress("UNUSED_VARIABLE")
	val currentDestination = navBackStackEntry?.destination

	NavigationBar {
		Items.forEach { item ->
			NavigationBarItem(
				// selected = currentDestination?.hierarchy?.any { it.hasRoute(item.destination) } == true,
				selected = false,
				onClick = {
					navController.navigate(item.destination) {
						popUpTo(navController.graph.findStartDestination().id) {
							saveState = true
						}
						launchSingleTop = true
						restoreState = true
					}
				},
				label = {
					Text(item.title)
				},
				icon = {
					Icon(item.icon, contentDescription = null)
				},
			)
		}
	}
}
