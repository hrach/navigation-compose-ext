package dev.hrach.navigation.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestinationBuilder
import androidx.navigation.NavDestinationDsl
import androidx.navigation.NavType
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * DSL for constructing a new [BottomSheetNavigator.Destination]
 */
@Suppress("UnnecessaryOptInAnnotation")
@NavDestinationDsl
public class BottomSheetNavigatorDestinationBuilder :
	NavDestinationBuilder<BottomSheetNavigator.Destination> {

	private val composeNavigator: BottomSheetNavigator
	private val content: @Composable (NavBackStackEntry) -> Unit

	public var securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit
	public var skipPartiallyExpanded: Boolean = true

	/**
	 * DSL for constructing a new [BottomSheetNavigator.Destination]
	 *
	 * @param navigator navigator used to create the destination
	 * @param route the destination's unique route
	 * @param content composable for the destination
	 */
	public constructor(
		navigator: BottomSheetNavigator,
		route: String,
		content: @Composable (NavBackStackEntry) -> Unit,
	) : super(navigator, route) {
		this.composeNavigator = navigator
		this.content = content
	}

	/**
	 * DSL for constructing a new [BottomSheetNavigator.Destination]
	 *
	 * @param navigator navigator used to create the destination
	 * @param route the destination's unique route from a [KClass]
	 * @param typeMap map of destination arguments' kotlin type [KType] to its respective custom
	 * [NavType]. May be empty if [route] does not use custom NavTypes.
	 * @param content composable for the destination
	 */
	public constructor(
		navigator: BottomSheetNavigator,
		route: KClass<*>,
		typeMap: Map<KType, @JvmSuppressWildcards NavType<*>>,
		content: @Composable (NavBackStackEntry) -> Unit,
	) : super(navigator, route, typeMap) {
		this.composeNavigator = navigator
		this.content = content
	}

	override fun instantiateDestination(): BottomSheetNavigator.Destination {
		return BottomSheetNavigator.Destination(composeNavigator, content)
	}

	override fun build(): BottomSheetNavigator.Destination {
		return super.build().also { destination ->
			destination.securePolicy = securePolicy
			destination.skipPartiallyExpanded = skipPartiallyExpanded
		}
	}
}
