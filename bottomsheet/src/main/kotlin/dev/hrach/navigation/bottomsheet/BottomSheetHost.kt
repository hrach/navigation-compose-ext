package dev.hrach.navigation.bottomsheet

import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.contentColorFor
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.LocalOwnersProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@ExperimentalMaterial3Api
@Composable
public fun BottomSheetHost(
	navigator: BottomSheetNavigator,
	modifier: Modifier = Modifier,
	sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
	shape: Shape = BottomSheetDefaults.ExpandedShape,
	containerColor: Color = BottomSheetDefaults.ContainerColor,
	contentColor: Color = contentColorFor(containerColor),
	tonalElevation: Dp = 0.dp,
	scrimColor: Color = BottomSheetDefaults.ScrimColor,
	dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
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
			modifier = modifier,
			sheetMaxWidth = sheetMaxWidth,
			shape = shape,
			containerColor = containerColor,
			contentColor = contentColor,
			tonalElevation = tonalElevation,
			scrimColor = scrimColor,
			dragHandle = dragHandle,
			saveableStateHolder = saveableStateHolder,
			targetBackStackEntry = backStackEntry,
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
	modifier: Modifier = Modifier,
	sheetMaxWidth: Dp,
	shape: Shape,
	containerColor: Color,
	contentColor: Color,
	tonalElevation: Dp,
	scrimColor: Color,
	dragHandle: @Composable (() -> Unit)?,
	saveableStateHolder: SaveableStateHolder,
	targetBackStackEntry: NavBackStackEntry?,
) {
	val destination = targetBackStackEntry?.destination as? BottomSheetNavigator.Destination
	val sheetState = rememberModalBottomSheetState(
		skipPartiallyExpanded = destination?.skipPartiallyExpanded ?: true,
	)

	@Suppress("ProduceStateDoesNotAssignValue") // false positive
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
		navigator = navigator,
		modifier = modifier,
		sheetMaxWidth = sheetMaxWidth,
		shape = shape,
		containerColor = containerColor,
		contentColor = contentColor,
		tonalElevation = tonalElevation,
		scrimColor = scrimColor,
		dragHandle = dragHandle,
		sheetState = sheetState,
		saveableStateHolder = saveableStateHolder,
		backStackEntry = backStackEntry ?: return,
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetHost(
	navigator: BottomSheetNavigator,
	modifier: Modifier = Modifier,
	sheetMaxWidth: Dp,
	shape: Shape,
	containerColor: Color,
	contentColor: Color,
	tonalElevation: Dp,
	scrimColor: Color,
	dragHandle: @Composable (() -> Unit)?,
	sheetState: SheetState,
	saveableStateHolder: SaveableStateHolder,
	backStackEntry: NavBackStackEntry,
) {
	LaunchedEffect(backStackEntry) {
		sheetState.show()
	}

	backStackEntry.LocalOwnersProvider(saveableStateHolder) {
		val destination = backStackEntry.destination as BottomSheetNavigator.Destination

		ModalBottomSheet(
			onDismissRequest = { navigator.dismiss(backStackEntry) },
			modifier = modifier,
			sheetState = sheetState,
			sheetMaxWidth = sheetMaxWidth,
			shape = shape,
			containerColor = containerColor,
			contentColor = contentColor,
			tonalElevation = tonalElevation,
			scrimColor = scrimColor,
			dragHandle = dragHandle,
			properties = ModalBottomSheetProperties(securePolicy = destination.securePolicy),
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
