package dev.hrach.navigation.demo

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.hrach.navigation.bottomsheet.BottomSheetHost
import dev.hrach.navigation.bottomsheet.BottomSheetNavigator
import dev.hrach.navigation.bottomsheet.bottomSheet
import dev.hrach.navigation.demo.screens.BottomSheet
import dev.hrach.navigation.demo.screens.Home
import dev.hrach.navigation.demo.screens.List
import dev.hrach.navigation.demo.screens.Modal1
import dev.hrach.navigation.demo.screens.Modal2
import dev.hrach.navigation.demo.screens.Profile
import dev.hrach.navigation.modalsheet.ModalSheetHost
import dev.hrach.navigation.modalsheet.ModalSheetNavigator
import dev.hrach.navigation.modalsheet.modalSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NavHost(
	navController: NavHostController,
	modalSheetNavigator: ModalSheetNavigator,
	bottomSheetNavigator: BottomSheetNavigator,
) {
	val density = LocalDensity.current
	NavHost(
		navController = navController,
		startDestination = Destinations.Home,
		enterTransition = { SharedXAxisEnterTransition(density) },
		exitTransition = { SharedXAxisExitTransition(density) },
		popEnterTransition = { SharedXAxisPopEnterTransition(density) },
		popExitTransition = { SharedXAxisPopExitTransition(density) },
	) {
		composable<Destinations.Home> { Home(navController) }
		composable<Destinations.List> { List() }
		composable<Destinations.Profile> { Profile() }
		modalSheet<Destinations.Modal1> { Modal1(navController) }
		modalSheet<Destinations.Modal2> { Modal2(navController) }
		bottomSheet<Destinations.BottomSheet> { BottomSheet(navController) }
	}
	ModalSheetHost(
		modalSheetNavigator = modalSheetNavigator,
		containerColor = MaterialTheme.colorScheme.background,
		enterTransition = { SharedYAxisEnterTransition(density) },
		exitTransition = { SharedYAxisExitTransition(density) },
	)
	BottomSheetHost(
		navigator = bottomSheetNavigator,
		shape = RoundedCornerShape(
			// optional, just an example of bottom sheet custom property
			topStart = CornerSize(12.dp),
			topEnd = CornerSize(12.dp),
			bottomStart = CornerSize(0.dp),
			bottomEnd = CornerSize(0.dp),
		),
	)
}

private val SharedXAxisEnterTransition: (Density) -> EnterTransition = { density ->
	fadeIn(animationSpec = tween(durationMillis = 210, delayMillis = 90, easing = LinearOutSlowInEasing)) +
		slideInHorizontally(animationSpec = tween(durationMillis = 300)) {
			with(density) { 30.dp.roundToPx() }
		}
}

private val SharedXAxisPopEnterTransition: (Density) -> EnterTransition = { density ->
	fadeIn(animationSpec = tween(durationMillis = 210, delayMillis = 90, easing = LinearOutSlowInEasing)) +
		slideInHorizontally(animationSpec = tween(durationMillis = 300)) {
			with(density) { (-30).dp.roundToPx() }
		}
}

private val SharedXAxisExitTransition: (Density) -> ExitTransition = { density ->
	fadeOut(animationSpec = tween(durationMillis = 90, easing = FastOutLinearInEasing)) +
		slideOutHorizontally(animationSpec = tween(durationMillis = 300)) {
			with(density) { (-30).dp.roundToPx() }
		}
}

private val SharedXAxisPopExitTransition: (Density) -> ExitTransition = { density ->
	fadeOut(animationSpec = tween(durationMillis = 90, easing = FastOutLinearInEasing)) +
		slideOutHorizontally(animationSpec = tween(durationMillis = 300)) {
			with(density) { 30.dp.roundToPx() }
		}
}

private val SharedYAxisEnterTransition: (Density) -> EnterTransition = { density ->
	fadeIn(animationSpec = tween(durationMillis = 210, delayMillis = 90, easing = LinearOutSlowInEasing)) +
		slideInVertically(animationSpec = tween(durationMillis = 300)) {
			with(density) { 30.dp.roundToPx() }
		}
}

private val SharedYAxisExitTransition: (Density) -> ExitTransition = { density ->
	fadeOut(animationSpec = tween(durationMillis = 210, delayMillis = 90, easing = LinearOutSlowInEasing)) +
		slideOutVertically(animationSpec = tween(durationMillis = 300)) {
			with(density) { 30.dp.roundToPx() }
		}
}
