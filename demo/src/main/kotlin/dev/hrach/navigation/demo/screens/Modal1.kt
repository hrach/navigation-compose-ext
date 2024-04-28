package dev.hrach.navigation.demo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import dev.hrach.navigation.demo.Destinations

@Composable
internal fun Modal1(navController: NavController) {
	Column(
		Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface)
			.windowInsetsPadding(WindowInsets.systemBars),
	) {
		Text("Modal 1")
		OutlinedButton(onClick = { navController.navigate(Destinations.Modal2) }) {
			Text("Modal 2")
		}
		OutlinedButton(onClick = { navController.navigate(Destinations.BottomSheet) }) {
			Text("BottomSheet")
		}
	}
}
