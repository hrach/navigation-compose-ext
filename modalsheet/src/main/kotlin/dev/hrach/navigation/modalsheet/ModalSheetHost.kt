package dev.hrach.navigation.modalsheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.LocalOwnersProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

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
	var progress by remember { mutableFloatStateOf(0f) }
	var inPredictiveBack by remember { mutableStateOf(false) }
	val zIndices = remember { mutableMapOf<String, Float>() }

	val saveableStateHolder = rememberSaveableStateHolder()

	val modalBackStack by modalSheetNavigator.backStack.collectAsState(listOf())
	val backStackEntry: NavBackStackEntry? = modalBackStack.lastOrNull()

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

	val transitionState = remember {
		// The state returned here cannot be nullable cause it produces the input of the
		// transitionSpec passed into the AnimatedContent and that must match the non-nullable
		// scope exposed by the transitions on the NavHost and composable APIs.
		SeekableTransitionState(backStackEntry)
	}
	val transitionsInProgress = modalSheetNavigator.transitionsInProgress.collectAsState().value
	val transition = rememberTransition(transitionState, label = "entry")
	val nothingToShow = transition.currentState == transition.targetState &&
		transition.currentState == null &&
		backStackEntry == null &&
		transitionsInProgress.isEmpty()

	if (inPredictiveBack) {
		LaunchedEffect(progress) {
			val previousEntry = modalBackStack.getOrNull(modalBackStack.size - 2)
			transitionState.seekTo(progress, previousEntry)
		}
	} else {
		LaunchedEffect(backStackEntry) {
			// This ensures we don't animate after the back gesture is cancelled and we
			// are already on the current state
			if (transitionState.currentState != backStackEntry) {
				transitionState.animateTo(backStackEntry)
			} else {
				// convert from nanoseconds to milliseconds
				val totalDuration = transition.totalDurationNanos / 1000000
				// When the predictive back gesture is cancel, we need to manually animate
				// the SeekableTransitionState from where it left off, to zero and then
				// snapTo the final position.
				animate(
					transitionState.fraction,
					0f,
					animationSpec = tween((transitionState.fraction * totalDuration).toInt()),
				) { value, _ ->
					this@LaunchedEffect.launch {
						if (value > 0) {
							// Seek the original transition back to the currentState
							transitionState.seekTo(value)
						}
						if (value == 0f) {
							// Once we animate to the start, we need to snap to the right state.
							transitionState.snapTo(backStackEntry)
						}
					}
				}
			}
		}
	}

	if (!nothingToShow) {
		val securePolicy = (backStackEntry?.destination as? ModalSheetNavigator.Destination)
			?.securePolicy
			?: SecureFlagPolicy.Inherit

		ModalSheetDialog(
			onPredictiveBack = onPredictBack@{ backEvent ->
				progress = 0f
				// early return: already animating backstack out, repeated back handling
				// probably reproducible only with slowed animations
				val currentBackStackEntry = modalBackStack.lastOrNull() ?: return@onPredictBack
				modalSheetNavigator.prepareForTransition(currentBackStackEntry)
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
					.background(if (transition.targetState == null || transition.currentState == null) Color.Transparent else containerColor),
				contentAlignment = Alignment.TopStart,
				transitionSpec = block@{
					@Suppress("UNCHECKED_CAST")
					val initialState = initialState ?: return@block ContentTransform(
						enterTransition(this as AnimatedContentTransitionScope<NavBackStackEntry>),
						fadeOut(), // irrelevant
						0f,
					)

					@Suppress("UNCHECKED_CAST")
					val targetState = targetState ?: return@block ContentTransform(
						fadeIn(), // irrelevant
						exitTransition(this as AnimatedContentTransitionScope<NavBackStackEntry>),
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
		if (transition.currentState == transition.targetState) {
			transitionsInProgress.forEach { entry -> modalSheetNavigator.onTransitionComplete(entry) }
			zIndices
				.filter { it.key != transition.targetState?.id }
				.forEach { zIndices.remove(it.key) }
		}
	}
}
