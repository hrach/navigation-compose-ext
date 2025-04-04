package dev.hrach.navigation.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.get
import kotlin.reflect.KType

public inline fun <reified T : Any> NavGraphBuilder.bottomSheet(
	typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
	deepLinks: List<NavDeepLink> = emptyList(),
	securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
	shouldDismissOnBackPress: Boolean = true,
	skipPartiallyExpanded: Boolean = true,
	confirmValueChange: Boolean = true,
	noinline content: @Composable (NavBackStackEntry) -> Unit,
) {
	destination(
		BottomSheetNavigatorDestinationBuilder(
			navigator = provider[BottomSheetNavigator::class],
			route = T::class,
			typeMap = typeMap,
			content = content,
		).apply {
			deepLinks.forEach { deepLink ->
				deepLink(deepLink)
			}
			this.securePolicy = securePolicy
			this.shouldDismissOnBackPress = shouldDismissOnBackPress
			this.skipPartiallyExpanded = skipPartiallyExpanded
			this.confirmValueChange = confirmValueChange
		},
	)
}

public fun NavGraphBuilder.bottomSheet(
	route: String,
	arguments: List<NamedNavArgument> = emptyList(),
	deepLinks: List<NavDeepLink> = emptyList(),
	securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
	shouldDismissOnBackPress: Boolean = true,
	skipPartiallyExpanded: Boolean = true,
	confirmValueChange: Boolean = true,
	content: @Composable (backstackEntry: NavBackStackEntry) -> Unit,
) {
	destination(
		BottomSheetNavigatorDestinationBuilder(
			navigator = provider[BottomSheetNavigator::class],
			route = route,
			content = content,
		).apply {
			arguments.forEach { (argumentName, argument) ->
				argument(argumentName, argument)
			}
			deepLinks.forEach { deepLink ->
				deepLink(deepLink)
			}
			this.securePolicy = securePolicy
			this.shouldDismissOnBackPress = shouldDismissOnBackPress
			this.skipPartiallyExpanded = skipPartiallyExpanded
			this.confirmValueChange = confirmValueChange
		},
	)
}
