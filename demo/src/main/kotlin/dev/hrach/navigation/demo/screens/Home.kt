package dev.hrach.navigation.demo.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import dev.hrach.navigation.demo.Destinations
import dev.hrach.navigation.results.NavigationResultEffect

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
internal fun Home(
	navController: NavController,
) {
	var bottomSheetResult by rememberSaveable { mutableIntStateOf(-1) }
	NavigationResultEffect<Destinations.BottomSheet.Result>(
		backStackEntry = remember(navController) { navController.getBackStackEntry<Destinations.Home>() },
		navController = navController,
	) { result ->
		bottomSheetResult = result.id
	}
	Home(
		navigate = navController::navigate,
		bottomSheetResult = bottomSheetResult,
	)
}

@Composable
private fun Home(
	navigate: (Any) -> Unit,
	bottomSheetResult: Int,
) {
	Column {
		Text("Home")

		OutlinedButton(onClick = { navigate(Destinations.IssueObjectThenClass) }) {
			Text("Modal 1")
		}

		OutlinedButton(onClick = { navigate(Destinations.BottomSheet) }) {
			Text("BottomSheet")
		}

		Text("BottomSheetResult: $bottomSheetResult")
	}
}
