package dev.hrach.navigation.modalsheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.LocalOwnersProvider
import kotlinx.coroutines.CancellationException

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
@Composable
public fun ModalSheetHost(
	modalSheetNavigator: ModalSheetNavigator,
	containerColor: Color,
	modifier: Modifier = Modifier,
	enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition) =
		{ fadeIn(animationSpec = tween(700)) },
	exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition) =
		{ fadeOut(animationSpec = tween(700)) },
	popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition) =
		enterTransition,
	popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition) =
		exitTransition,
	sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards SizeTransform?)? =
		null,
) {
	val modalBackStack by modalSheetNavigator.backStack.collectAsState(listOf())

	var progress by remember { mutableFloatStateOf(0f) }
	var inPredictiveBack by remember { mutableStateOf(false) }

	val zIndices = remember { mutableMapOf<String, Float>() }

	val saveableStateHolder = rememberSaveableStateHolder()

	val visibleEntries = rememberVisibleList(modalBackStack)
	visibleEntries.PopulateVisibleList(modalBackStack)

	val backStackEntry: NavBackStackEntry? = if (LocalInspectionMode.current) {
		modalSheetNavigator.backStack.collectAsState(emptyList()).value.lastOrNull()
	} else {
		visibleEntries.lastOrNull()
	}

	val finalEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
		val targetDestination = targetState.destination as ModalSheetNavigator.Destination
		if (modalSheetNavigator.isPop.value) {
			targetDestination.hierarchy.firstNotNullOfOrNull { destination ->
				null // destination.createPopEnterTransition(this)
			} ?: popEnterTransition.invoke(this)
		} else {
			targetDestination.hierarchy.firstNotNullOfOrNull { destination ->
				null // destination.createEnterTransition(this)
			} ?: enterTransition.invoke(this)
		}
	}
	val finalExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
		val initialDestination = initialState.destination as ModalSheetNavigator.Destination

		if (modalSheetNavigator.isPop.value) {
			initialDestination.hierarchy.firstNotNullOfOrNull { destination ->
				null // destination.createPopExitTransition(this)
			} ?: popExitTransition.invoke(this)
		} else {
			initialDestination.hierarchy.firstNotNullOfOrNull { destination ->
				null // destination.createExitTransition(this)
			} ?: exitTransition.invoke(this)
		}
	}
	val finalSizeTransform: AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform? = {
		val targetDestination = targetState.destination as ModalSheetNavigator.Destination

		targetDestination.hierarchy.firstNotNullOfOrNull { destination ->
			null // destination.createSizeTransform(this)
		} ?: sizeTransform?.invoke(this)
	}

	val transition = updateTransition(backStackEntry, label = "entry")
	val nothingToShow = transition.currentState == transition.targetState &&
		transition.currentState == null &&
		backStackEntry == null
	if (!nothingToShow) {
		val securePolicy = (backStackEntry?.destination as? ModalSheetNavigator.Destination)
			?.securePolicy
			?: SecureFlagPolicy.Inherit

		ModalSheetDialog(
			onPredictiveBack = { backEvent ->
				progress = 0f
				val currentBackStackEntry = modalBackStack.lastOrNull()
				modalSheetNavigator.prepareForTransition(currentBackStackEntry!!)
				val previousEntry = modalBackStack.getOrNull(modalBackStack.size - 2)
				if (previousEntry != null) {
					modalSheetNavigator.prepareForTransition(previousEntry)
				}
				try {
					backEvent.collect {
						inPredictiveBack = true
						progress = it.progress
					}
					inPredictiveBack = false
					modalSheetNavigator.popBackStack(currentBackStackEntry, false)
				} catch (e: CancellationException) {
					inPredictiveBack = false
				}
			},
			securePolicy = securePolicy,
		) {
			transition.AnimatedContent(
				modifier = modifier
					.background(if (transition.targetState == null) Color.Unspecified else containerColor),
				contentAlignment = Alignment.TopStart,
				transitionSpec = block@{
					val initialState = initialState ?: return@block ContentTransform(
						fadeIn(),
						fadeOut(), // irrelevant
						0f,
					)
					val targetState = targetState ?: return@block ContentTransform(
						fadeIn(), // irrelevant
						fadeOut(),
						0f,
					)

					val initialZIndex =
						zIndices[initialState.id] ?: 0f.also { zIndices[initialState.id] = 0f }
					val targetZIndex = when {
						targetState.id == initialState.id -> initialZIndex
						modalSheetNavigator.isPop.value -> initialZIndex - 1f
						else -> initialZIndex + 1f
					}.also { zIndices[targetState.id] = it }

					// cast to proper type as null is already handled
					@Suppress("UNCHECKED_CAST")
					this as AnimatedContentTransitionScope<NavBackStackEntry>
					ContentTransform(
						targetContentEnter = finalEnter(this),
						initialContentExit = finalExit(this),
						targetContentZIndex = targetZIndex,
						sizeTransform = finalSizeTransform(this),
					)
				},
			) { currentEntry ->
				if (currentEntry == null) {
					Box(Modifier.fillMaxSize()) {}
					return@AnimatedContent
				}

				currentEntry.LocalOwnersProvider(saveableStateHolder) {
					(currentEntry.destination as ModalSheetNavigator.Destination)
						.content(this, currentEntry)
				}
				DisposableEffect(currentEntry) {
					onDispose {
						modalSheetNavigator.onTransitionComplete(currentEntry)
					}
				}
			}
		}
	}
	LaunchedEffect(transition.currentState, transition.targetState) {
		if (transition.currentState == transition.targetState && backStackEntry != null) {
			modalSheetNavigator.onTransitionComplete(backStackEntry)
			zIndices
				.filter { it.key != transition.targetState?.id }
				.forEach { zIndices.remove(it.key) }
		}
	}
}

@Suppress("ComposeUnstableCollections")
@Composable
internal fun MutableList<NavBackStackEntry>.PopulateVisibleList(
	transitionsInProgress: List<NavBackStackEntry>,
) {
	val isInspecting = LocalInspectionMode.current
	transitionsInProgress.forEach { entry ->
		DisposableEffect(entry.lifecycle) {
			val observer = LifecycleEventObserver { _, event ->
				// show dialog in preview
				if (isInspecting && !contains(entry)) {
					add(entry)
				}
				// ON_START -> add to visibleBackStack, ON_STOP -> remove from visibleBackStack
				if (event == Lifecycle.Event.ON_START) {
					// We want to treat the visible lists as sets, but we want to keep
					// the functionality of mutableStateListOf() so that we recompose in response
					// to adds and removes.
					if (!contains(entry)) {
						add(entry)
					}
				}
				if (event == Lifecycle.Event.ON_STOP) {
					remove(entry)
				}
			}
			entry.lifecycle.addObserver(observer)
			onDispose {
				entry.lifecycle.removeObserver(observer)
			}
		}
	}
}

@Composable
internal fun rememberVisibleList(
	transitionsInProgress: List<NavBackStackEntry>,
): SnapshotStateList<NavBackStackEntry> {
	// show dialog in preview
	val isInspecting = LocalInspectionMode.current
	return remember(transitionsInProgress) {
		mutableStateListOf<NavBackStackEntry>().also {
			it.addAll(
				transitionsInProgress.filter { entry ->
					if (isInspecting) {
						true
					} else {
						entry.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
					}
				},
			)
		}
	}
}
