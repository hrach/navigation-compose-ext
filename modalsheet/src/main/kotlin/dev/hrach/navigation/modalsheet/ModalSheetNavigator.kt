package dev.hrach.navigation.modalsheet

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.navigation.FloatingWindow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import dev.hrach.navigation.modalsheet.ModalSheetNavigator.Destination

@Navigator.Name("ModalSheetNavigator")
public class ModalSheetNavigator : Navigator<Destination>() {
	internal val backStack get() = state.backStack
	internal val transitionsInProgress get() = state.transitionsInProgress

	internal val isPop = mutableStateOf(false)

	override fun navigate(
		entries: List<NavBackStackEntry>,
		navOptions: NavOptions?,
		navigatorExtras: Extras?,
	) {
		entries.forEach { entry ->
			state.pushWithTransition(entry)
		}
		isPop.value = false
	}

	override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
		state.popWithTransition(popUpTo, savedState)
		isPop.value = true
	}

	public fun prepareForTransition(entry: NavBackStackEntry) {
		state.prepareForTransition(entry)
	}

	internal fun onTransitionComplete(entry: NavBackStackEntry) {
		state.markTransitionComplete(entry)
	}

	override fun createDestination(): Destination =
		Destination(this) {}

	@NavDestination.ClassType(Composable::class)
	public class Destination(
		navigator: ModalSheetNavigator,
		internal val content: @Composable AnimatedContentScope.(@JvmSuppressWildcards NavBackStackEntry) -> Unit,
	) : NavDestination(navigator), FloatingWindow {
		internal var securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit

		internal var enterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
			null

		internal var exitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
			null

		internal var popEnterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
			null

		internal var popExitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
			null

		internal var sizeTransform: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)? =
			null
	}
}
