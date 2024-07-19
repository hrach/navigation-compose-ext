package dev.hrach.navigation.bottomsheet

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.LocalOwnersProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Composable
public fun BottomSheetHost(
	navigator: BottomSheetNavigator,
	modifier: Modifier = Modifier,
) {
	val saveableStateHolder = rememberSaveableStateHolder()

	val sheetBackStackState by remember {
		navigator.backStack.zipWithPreviousCount()
	}.collectAsStateWithLifecycle(Pair(0, emptyList()))

	val count by remember {
		derivedStateOf {
			maxOf(
				sheetBackStackState.first,
				sheetBackStackState.second.count(),
			)
		}
	}

	repeat(count) { i ->
		val backStackEntry = sheetBackStackState.second.getOrNull(i)
		BottomSheetHost(
			navigator = navigator,
			saveableStateHolder = saveableStateHolder,
			targetBackStackEntry = backStackEntry,
			modifier = modifier,
		)
	}
}

private fun Flow<List<NavBackStackEntry>>.zipWithPreviousCount(): Flow<Pair<Int, List<NavBackStackEntry>>> =
	flow {
		var previous = 0
		collect { value ->
			emit(Pair(previous, value))
			previous = value.count()
		}
	}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetHost(
	navigator: BottomSheetNavigator,
	saveableStateHolder: SaveableStateHolder,
	targetBackStackEntry: NavBackStackEntry?,
	modifier: Modifier = Modifier,
) {
	val destination = targetBackStackEntry?.destination as? BottomSheetNavigator.Destination
	val sheetState = rememberModalBottomSheetState(
		skipPartiallyExpanded = destination?.skipPartiallyExpanded ?: true,
	)
	val backStackEntry by produceState<NavBackStackEntry?>(
		initialValue = null,
		key1 = targetBackStackEntry,
	) {
		try {
			sheetState.hide()
		} catch (_: CancellationException) {
			// We catch but ignore possible cancellation exceptions as we don't want
			// them to bubble up and cancel the whole produceState coroutine
		} finally {
			value = targetBackStackEntry
		}
	}

	BottomSheetHost(
		sheetState = sheetState,
		navigator = navigator,
		saveableStateHolder = saveableStateHolder,
		backStackEntry = backStackEntry ?: return,
		modifier = modifier
	)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BottomSheetHost(
	sheetState: SheetState,
	navigator: BottomSheetNavigator,
	saveableStateHolder: SaveableStateHolder,
	backStackEntry: NavBackStackEntry,
	modifier: Modifier = Modifier,
) {
	LaunchedEffect(backStackEntry) {
		sheetState.show()
	}

	backStackEntry.LocalOwnersProvider(saveableStateHolder) {
		val destination = backStackEntry.destination as BottomSheetNavigator.Destination

		ModalBottomSheet(
			sheetState = sheetState,
			properties = ModalBottomSheetProperties(securePolicy = destination.securePolicy),
			onDismissRequest = { navigator.dismiss(backStackEntry) },
			modifier = modifier
		) {
			LaunchedEffect(backStackEntry) {
				navigator.onTransitionComplete(backStackEntry)
			}
			DisposableEffect(backStackEntry) {
				onDispose {
					navigator.onTransitionComplete(backStackEntry)
				}
			}
			destination.content(backStackEntry)
		}
	}
}
