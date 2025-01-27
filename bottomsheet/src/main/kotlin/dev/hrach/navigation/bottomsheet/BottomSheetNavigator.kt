package dev.hrach.navigation.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.navigation.FloatingWindow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

@Navigator.Name("M3BottomSheetNavigator")
public class BottomSheetNavigator : Navigator<BottomSheetNavigator.Destination>() {

	/**
	 * Get the back stack from the [state].
	 */
	internal val backStack get() = state.backStack

	/**
	 * Dismiss the sheet destination associated with the given [backStackEntry].
	 */
	internal fun dismiss(backStackEntry: NavBackStackEntry) {
		state.popWithTransition(backStackEntry, false)
	}

	override fun navigate(
		entries: List<NavBackStackEntry>,
		navOptions: NavOptions?,
		navigatorExtras: Extras?,
	) {
		entries.forEach { entry ->
			state.pushWithTransition(entry)
		}
	}

	override fun createDestination(): Destination {
		return Destination(this) { }
	}

	override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
		state.popWithTransition(popUpTo, savedState)
	}

	internal fun onTransitionComplete(entry: NavBackStackEntry) {
		state.markTransitionComplete(entry)
	}

	/**
	 * NavDestination specific to [BottomSheetNavigator]
	 */
	@NavDestination.ClassType(Composable::class)
	public class Destination(
		navigator: BottomSheetNavigator,
		internal val content: @Composable (NavBackStackEntry) -> Unit,
	) : NavDestination(navigator), FloatingWindow {
		internal var securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit
		internal var shouldDismissOnBackPress: Boolean = true
		internal var confirmValueChange: Boolean = true
		internal var skipPartiallyExpanded: Boolean = true
	}
}
