package dev.hrach.navigation.results

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Registers an effect for processing a dialog destination's result.
 *
 * To work properly, obtain the "current" destinations backstack using [NavController.getBackStackEntry].
 *
 * !!! DO NOT USE !!! [NavController.currentBackStackEntry] !!! DO NOT USE !!!
 * as it may provide a dialog's entry instead of the source's entry that should receive
 * e.g. the dialog's result. See the [official documentation](https://developer.android.com/guide/navigation/use-graph/programmatic#additional_considerations).
 *
 * ```kotlin
 * NavigationResultEffect(
 *     backStackEntry = remember(navController) { navController.getBackStackEntry<YourDestination>() },
 *     navController = navController,
 * ) { result: Destinations.Dialog.Result ->
 *     // process result
 * }
 * ```
 */
@Composable
public inline fun <reified R : Any> NavigationResultEffect(
	backStackEntry: NavBackStackEntry,
	navController: NavController,
	noinline block: (R) -> Unit,
) {
	NavigationResultEffectImpl(
		backStackEntry = backStackEntry,
		navController = navController,
		resultSerializer = serializer<R>(),
		block = block,
	)
}

/**
 * Implementation of ResultEffect. Use [NavigationResultEffect].
 */
@OptIn(ExperimentalSerializationApi::class)
@PublishedApi
@Composable
internal fun <R : Any> NavigationResultEffectImpl(
	backStackEntry: NavBackStackEntry,
	navController: NavController,
	resultSerializer: KSerializer<R>,
	block: (R) -> Unit,
) {
	DisposableEffect(navController) {
		// The implementation is based on the official documentation of the Result sharing.
		// It takes into consideration the possibility of a dialog usage (see the docs).
		// https://developer.android.com/guide/navigation/navigation-programmatic#additional_considerations
		val resultKey = resultSerializer.descriptor.serialName + "_result"
		val observer = LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_RESUME && backStackEntry.savedStateHandle.contains(resultKey)) {
				val result = backStackEntry.savedStateHandle.remove<String>(resultKey)!!
				val decoded = Json.decodeFromString(resultSerializer, result)
				block(decoded)
			}
		}
		backStackEntry.lifecycle.addObserver(observer)
		onDispose {
			backStackEntry.lifecycle.removeObserver(observer)
		}
	}
}

/**
 * Sets a result for the previous backstack entry.
 *
 * The result type has to be KotlinX Serializable.
 */
public inline fun <reified R : Any> NavController.setResult(
	data: R,
) {
	setResultImpl(data, serializer())
}

@OptIn(ExperimentalSerializationApi::class)
@PublishedApi
internal fun <R : Any> NavController.setResultImpl(
	data: R,
	resultSerializer: KSerializer<R>,
) {
	val result = Json.encodeToString(resultSerializer, data)
	val resultKey = resultSerializer.descriptor.serialName + "_result"
	previousBackStackEntry?.savedStateHandle?.set(resultKey, result)
}
