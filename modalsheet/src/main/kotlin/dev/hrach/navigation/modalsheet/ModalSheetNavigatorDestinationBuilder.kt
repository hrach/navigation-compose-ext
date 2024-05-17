package dev.hrach.navigation.modalsheet

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestinationBuilder
import androidx.navigation.NavDestinationDsl
import androidx.navigation.NavType
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * DSL for constructing a new [ModalSheetNavigator.Destination]
 */
@Suppress("UnnecessaryOptInAnnotation")
@NavDestinationDsl
public class ModalSheetNavigatorDestinationBuilder :
	NavDestinationBuilder<ModalSheetNavigator.Destination> {

	private val composeNavigator: ModalSheetNavigator
	private val content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)

	public var securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit

	public var enterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
		null

	public var exitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
		null

	public var popEnterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
		null

	public var popExitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
		null

	public var sizeTransform: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)? =
		null

	/**
	 * DSL for constructing a new [ModalSheetNavigator.Destination]
	 *
	 * @param navigator navigator used to create the destination
	 * @param route the destination's unique route
	 * @param content composable for the destination
	 */
	public constructor(
		navigator: ModalSheetNavigator,
		route: String,
		content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
	) : super(navigator, route) {
		this.composeNavigator = navigator
		this.content = content
	}

	/**
	 * DSL for constructing a new [ModalSheetNavigator.Destination]
	 *
	 * @param navigator navigator used to create the destination
	 * @param route the destination's unique route from a [KClass]
	 * @param typeMap map of destination arguments' kotlin type [KType] to its respective custom
	 * [NavType]. May be empty if [route] does not use custom NavTypes.
	 * @param content composable for the destination
	 */
	public constructor(
		navigator: ModalSheetNavigator,
		route: KClass<*>,
		typeMap: Map<KType, @JvmSuppressWildcards NavType<*>>,
		content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
	) : super(navigator, route, typeMap) {
		this.composeNavigator = navigator
		this.content = content
	}

	override fun instantiateDestination(): ModalSheetNavigator.Destination {
		return ModalSheetNavigator.Destination(composeNavigator, content)
	}

	override fun build(): ModalSheetNavigator.Destination {
		return super.build().also { destination ->
			destination.enterTransition = enterTransition
			destination.exitTransition = exitTransition
			destination.popEnterTransition = popEnterTransition
			destination.popExitTransition = popExitTransition
			destination.sizeTransform = sizeTransform
		}
	}
}
