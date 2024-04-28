package dev.hrach.navigation.demo.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import dev.hrach.navigation.demo.Destinations

@Composable
internal fun Home(
	navController: NavController,
) {
	Column {
		Text("Home")

		OutlinedButton(onClick = { navController.navigate(Destinations.Modal1) }) {
			Text("Modal 1")
		}

		OutlinedButton(onClick = { navController.navigate(Destinations.BottomSheet) }) {
			Text("BottomSheet")
		}
	}
}
