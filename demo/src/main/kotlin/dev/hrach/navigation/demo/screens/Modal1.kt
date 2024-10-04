package dev.hrach.navigation.demo.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.hrach.navigation.demo.Destinations
import dev.hrach.navigation.results.NavigationResultEffect

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
internal fun Modal1(navController: NavController) {
	var bottomSheetResult by rememberSaveable { mutableIntStateOf(-1) }
	NavigationResultEffect<Destinations.BottomSheet.Result>(
		backStackEntry = remember(navController) { navController.getBackStackEntry<Destinations.Modal1>() },
		navController = navController,
	) { result ->
		bottomSheetResult = result.id
	}
	Modal1(
		navigate = navController::navigate,
		close = navController::popBackStack,
		bottomSheetResult = bottomSheetResult,
	)
}

@Composable
private fun Modal1(
	navigate: (Any) -> Unit,
	close: () -> Unit,
	bottomSheetResult: Int,
) {
	var disableBackHandling by rememberSaveable { mutableStateOf(false) }
	BackHandler(disableBackHandling) {
		// no-op
	}

	Surface(
		color = MaterialTheme.colorScheme.inverseSurface,
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.windowInsetsPadding(WindowInsets.systemBars),
		) {
			Text("Modal 1")
			OutlinedButton(onClick = { navigate(Destinations.Modal2) }) {
				Text("Modal 2")
			}
			OutlinedButton(onClick = { navigate(Destinations.BottomSheet) }) {
				Text("BottomSheet")
			}
			Text("BottomSheetResult: $bottomSheetResult")

			Spacer(Modifier.height(32.dp))

			Row(verticalAlignment = Alignment.CenterVertically) {
				Text("Disable back handling")
				Switch(disableBackHandling, onCheckedChange = { disableBackHandling = it })
			}

			Spacer(Modifier.height(32.dp))
			OutlinedButton(onClick = close) {
				Text("Close")
			}
		}
	}
}
