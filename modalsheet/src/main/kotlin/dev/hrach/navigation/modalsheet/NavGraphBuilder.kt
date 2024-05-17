package dev.hrach.navigation.modalsheet

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.get
import kotlin.reflect.KType

public inline fun <reified T : Any> NavGraphBuilder.modalSheet(
	typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
	deepLinks: List<NavDeepLink> = emptyList(),
	securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
	noinline enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
	@JvmSuppressWildcards EnterTransition?)? = null,
	noinline exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
	@JvmSuppressWildcards ExitTransition?)? = null,
	noinline popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
	@JvmSuppressWildcards EnterTransition?)? = enterTransition,
	noinline popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
	@JvmSuppressWildcards ExitTransition?)? = exitTransition,
	noinline sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
	@JvmSuppressWildcards SizeTransform?)? = null,
	noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
	destination(
		ModalSheetNavigatorDestinationBuilder(
			provider[ModalSheetNavigator::class],
			T::class,
			typeMap,
			content,
		).apply {
			deepLinks.forEach { deepLink ->
				deepLink(deepLink)
			}
			this.securePolicy = securePolicy
			this.enterTransition = enterTransition
			this.exitTransition = exitTransition
			this.popEnterTransition = popEnterTransition
			this.popExitTransition = popExitTransition
			this.sizeTransform = sizeTransform
		},
	)
}

public fun NavGraphBuilder.modalSheet(
	route: String,
	arguments: List<NamedNavArgument> = emptyList(),
	deepLinks: List<NavDeepLink> = emptyList(),
	securePolicy: SecureFlagPolicy,
	enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
	@JvmSuppressWildcards EnterTransition?)? = null,
	exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
	@JvmSuppressWildcards ExitTransition?)? = null,
	popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
	@JvmSuppressWildcards EnterTransition?)? = enterTransition,
	popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
	@JvmSuppressWildcards ExitTransition?)? = exitTransition,
	sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() ->
	@JvmSuppressWildcards SizeTransform?)? = null,
	content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
	destination(
		ModalSheetNavigatorDestinationBuilder(
			provider[ModalSheetNavigator::class],
			route,
			content,
		).apply {
			arguments.forEach { (argumentName, argument) ->
				argument(argumentName, argument)
			}
			deepLinks.forEach { deepLink ->
				deepLink(deepLink)
			}
			this.securePolicy = securePolicy
			this.enterTransition = enterTransition
			this.exitTransition = exitTransition
			this.popEnterTransition = popEnterTransition
			this.popExitTransition = popExitTransition
			this.sizeTransform = sizeTransform
		},
	)
}
